package com.unicornutterances.cms.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

const val OAUTH_GITHUB = "auth-oauth-github"

fun Application.configureOAuth() {
    authentication {
        oauth(OAUTH_GITHUB) {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GITHUB_CLIENT_ID"),
                    clientSecret = System.getenv("GITHUB_CLIENT_SECRET"),
                    defaultScopes = listOf()
                )
            }
            client = HttpClient(Apache)
        }
    }

    routing {
        route("/oauth/github") {
            authenticate(OAUTH_GITHUB) {
                get("login") {
                    call.respondRedirect("/oauth/github/callback")
                }

                get("callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()
                        ?: throw BadRequestException("No OAuth2 provided")

                    call.sessions.set(GitHubSession(principal.accessToken))
                    call.respondRedirect("/hello")
                }
            }
        }
    }
}

data class GitHubSession(val accessToken: String)
