package com.playfulprogramming.cms.backends.flyio

import com.playfulprogramming.cms.backends.flyio.data.FlyMachine
import com.playfulprogramming.cms.config.EnvConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.koin.dsl.module

val flyClientModule = module {
    factory {
        val env = get<EnvConfig>()
        env.flyApiUrl
            ?.takeIf { it.isNotEmpty() }
            ?.let { FlyClientImpl(it, env) }
            ?: FlyClientNoOp()
    }
}

interface FlyClient {
    suspend fun getMachines(appName: String): List<FlyMachine>
    suspend fun startMachine(appName: String, machineId: String)
}

private class FlyClientNoOp : FlyClient {
    override suspend fun getMachines(appName: String): List<FlyMachine> = emptyList()
    override suspend fun startMachine(appName: String, machineId: String) = Unit
}

private class FlyClientImpl(
    private val baseUrl: String,
    private val env: EnvConfig,
) : FlyClient {

    private val ktorClient = HttpClient(Apache) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                @OptIn(ExperimentalSerializationApi::class)
                namingStrategy = JsonNamingStrategy.SnakeCase
            })
        }
    }

    override suspend fun getMachines(appName: String): List<FlyMachine> {
        val call = ktorClient.get("$baseUrl/v1/apps/${appName}/machines") {
            header("Authorization", "Bearer ${env.flyApiToken}")
        }
        return call.body()
    }

    override suspend fun startMachine(appName: String, machineId: String) {
        ktorClient.post("$baseUrl/v1/apps/${appName}/machines/${machineId}/start") {
            header("Authorization", "Bearer ${env.flyApiToken}")
        }
    }

}
