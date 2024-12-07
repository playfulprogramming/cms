package com.playfulprogramming.cms.tasks

import com.playfulprogramming.cms.tasks.data.PostImageRequest
import com.playfulprogramming.cms.tasks.data.PostImageResponse
import com.playfulprogramming.cms.tasks.data.UrlMetadataRequest
import com.playfulprogramming.cms.tasks.data.UrlMetadataResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.apache.commons.codec.digest.DigestUtils
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.ext.inject

private inline fun <reified Request: Any, reified Response: Any> Routing.declareTaskRoute(
    name: String,
    crossinline requestToId: (Request) -> String,
) {
    val taskService by inject<TaskService>()

    post("/tasks/$name") {
        val request: Request = call.receive()
        val result = taskService.getOrCreate(
            id = "$name/${requestToId(request)}",
            data = Json.encodeToJsonElement(request),
        )
        when {
            result == null -> call.respond(HttpStatusCode.Accepted)
            result.output == null -> call.respond(HttpStatusCode.NotFound)
            else -> call.respond(
                Json.decodeFromString<Response>(result.output)
            )
        }
    }
}

fun Routing.configureTaskRoutes() {
    declareTaskRoute<UrlMetadataRequest, UrlMetadataResponse>(
        name = "url-metadata",
        requestToId = { DigestUtils.md5Hex(it.url) },
    )

    declareTaskRoute<PostImageRequest, PostImageResponse>(
        name = "post-image",
        requestToId = { it.slug },
    )
}

val tasksModule = module {
    singleOf(::TaskServiceImpl) bind TaskService::class
}
