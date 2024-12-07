package com.playfulprogramming.cms.github.client

import com.playfulprogramming.cms.github.client.data.GHUser
import com.playfulprogramming.cms.github.client.data.GHUserToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface GitHubClient {
    suspend fun getUser(authorization: GHUserToken): GHUser
}

class GitHubClientImpl(
    private val restUrl: String = "https://api.github.com",
) : GitHubClient {

    private val ktorClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun getUser(authorization: GHUserToken): GHUser {
        val call = ktorClient.get("$restUrl/user") {
            header("Accept", "application/vnd.github+json")
            header("Authorization", "Bearer ${authorization.token}")
        }
        return call.body()
    }

}