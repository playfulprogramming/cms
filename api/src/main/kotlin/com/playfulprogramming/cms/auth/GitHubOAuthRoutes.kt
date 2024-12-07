package com.playfulprogramming.cms.auth

import com.playfulprogramming.cms.config.EnvConfig
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.koin.ktor.ext.inject

const val AUTH_SESSION = "auth-session"
const val AUTH_GITHUB = "auth-oauth-github"

fun Application.configureGitHubOAuth() {
    val env: EnvConfig by inject()
    val gitHubOAuthService: GitHubOAuthService by inject()

    install(Sessions) {
        val secretEncryptKey = hex(env.secretEncryptKey)
        val secretSignKey = hex(env.secretSignKey)
        cookie<SessionData>(AUTH_SESSION) {
            cookie.httpOnly = true
            cookie.secure = env.apiUrl.startsWith("https://")
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    authentication {
        session<SessionData>(AUTH_SESSION) {
            validate { session ->
                val isValid = gitHubOAuthService.verifySession(session)
                if (isValid) session else null
            }
        }
        oauth(AUTH_GITHUB) {
            urlProvider = { "${env.apiUrl}/auth/github/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    requestMethod = HttpMethod.Post,
                    clientId = env.githubClientId,
                    clientSecret = env.githubClientSecret,
                    defaultScopes = listOf()
                )
            }
            client = HttpClient(Apache)
        }
    }

    routing {
        route("/auth/github") {
            authenticate(AUTH_GITHUB) {
                get("login") {
                    // Redirects to 'authorizeUrl' automatically
                }

                get("callback") {
                    val principal: OAuthAccessTokenResponse.OAuth2 = call.authentication.principal()
                        ?: throw BadRequestException("No OAuth2 provided")

                    // Validate the resulting authToken and enter it into the database
                    val session = gitHubOAuthService.handleOAuthCallback(principal)
                    // Return the resulting session cookie
                    call.sessions.set(AUTH_SESSION, session)

                    // Redirect the user back to the app
                    call.respondRedirect(env.clientUrl)
                }
            }
        }

        authenticate(AUTH_SESSION) {
            get("/auth/user") {
                val session = call.principal<SessionData>()
                    ?: throw BadRequestException("Missing SessionData principal")

                val user = gitHubOAuthService.getSessionUser(session)
                call.respond(user)
            }
        }

        get("/auth/logout") {
            call.sessions.clear(AUTH_SESSION)
            call.respondRedirect(env.clientUrl)
        }
    }
}
