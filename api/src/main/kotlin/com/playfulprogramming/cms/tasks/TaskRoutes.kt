package com.playfulprogramming.cms.tasks

import com.playfulprogramming.cms.backends.flyio.FlyClient
import com.playfulprogramming.cms.config.EnvConfig
import com.playfulprogramming.cms.tasks.data.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.apache.commons.codec.digest.DigestUtils
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.time.Duration.Companion.seconds

private val json = Json {
    encodeDefaults = true
    explicitNulls = false
}

private suspend inline fun <reified Request: Any, reified Response: Any> invokeTask(
    taskService: TaskService,
    task: String,
    id: String,
    request: Request,
): TaskResult<Response> {
    val result = taskService.getOrCreate(
        task = task,
        id = id,
        data = json.encodeToJsonElement(request),
    )

    return if (result != null) {
        result.output
            ?.let { json.decodeFromString<Response>(it) }
            ?.let { TaskResult.Success(it) }
            ?: TaskResult.Failed()
    } else {
        TaskResult.Accepted()
    }
}

class TaskRoutes(
    private val taskService: TaskService,
    private val flyClient: FlyClient,
    private val env: EnvConfig,
    application: Application,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var startedRandomAt: Instant = Instant.DISTANT_PAST
    @OptIn(DelicateCoroutinesApi::class)
    private fun startRandomFlyWorker() {
        val now = Clock.System.now()
        if (now - startedRandomAt < 1.seconds)
            return

        startedRandomAt = now

        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                // Find a suspended or stopped machine
                val machine = flyClient.getMachines(env.flyWorkerAppName)
                    .filter { it.state in arrayOf("suspended", "stopped") }
                    .randomOrNull()

                if (machine != null) {
                    // If found, send a command to start it
                    logger.info("Starting machine id=${machine.id} to run task")
                    flyClient.startMachine(env.flyWorkerAppName, machine.id)
                }
            }
                .exceptionOrNull()
                ?.let { logger.error("Error starting fly worker", it) }
        }
    }

    init {
        application.routing {
            post("/tasks/url-metadata") {
                val request: UrlMetadataRequest = call.receive()
                val url = URI.create(request.url)

                // remove any query/fragment or casing from the URL
                val normalizedUrl = URI(url.scheme.lowercase(), url.host.lowercase(), url.path, null)
                val normalizedRequest = request.copy(url = normalizedUrl.toString())

                val task = invokeTask<UrlMetadataRequest, UrlMetadataResponse>(
                    taskService = taskService,
                    task = "url-metadata",
                    id = DigestUtils.md5Hex(normalizedRequest.url),
                    request = normalizedRequest,
                )

                when (task) {
                    is TaskResult.Accepted -> {
                        startRandomFlyWorker()
                        call.respond(HttpStatusCode.Accepted)
                    }
                    is TaskResult.Failed -> call.respond(HttpStatusCode.NotFound)
                    is TaskResult.Success -> call.respond(
                        UrlMetadataResponse(
                            title = task.result.title,
                            icon = task.result.icon?.let { "${env.s3PublicUrl}/${env.s3Bucket}/$it" },
                            banner = task.result.banner?.let { "${env.s3PublicUrl}/${env.s3Bucket}/$it" },
                        )
                    )
                }
            }

            post("/tasks/post-image") {
                val request: PostImageRequest = call.receive()
                val task = invokeTask<PostImageRequest, PostImageResponse>(
                    taskService = taskService,
                    task = "post-image",
                    id = request.slug,
                    request = request,
                )

                when (task) {
                    is TaskResult.Accepted -> {
                        startRandomFlyWorker()
                        call.respond(HttpStatusCode.Accepted)
                    }
                    is TaskResult.Failed -> call.respond(HttpStatusCode.NotFound)
                    is TaskResult.Success -> call.respond(
                        PostImageResponse(
                            bannerImage = "${env.s3PublicUrl}/${env.s3Bucket}/${task.result.bannerImage}",
                            socialImage = "${env.s3PublicUrl}/${env.s3Bucket}/${task.result.socialImage}",
                        )
                    )
                }
            }
        }
    }
}

val tasksModule = module {
    singleOf(::TaskServiceImpl) bind TaskService::class
    singleOf(::TaskRoutes) withOptions { createdAtStart() }
}
