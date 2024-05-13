package com.unicornutterances.cms.auth

import com.unicornutterances.cms.github.client.GitHubClient
import com.unicornutterances.cms.github.client.data.GHUserToken
import com.unicornutterances.cms.sql.Database
import com.unicornutterances.cms.sql.GitHubUser
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Hex
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.security.SecureRandom
import java.time.OffsetDateTime

class GitHubOAuthService(
    private val database: Database,
    private val gitHubClient: GitHubClient,
) {

    private val secureRandom = SecureRandom()
    private val arg2SpringSecurity = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    private fun createSessionId(): String {
        val arr = ByteArray(32)
        secureRandom.nextBytes(arr)
        return Hex.encodeHexString(arr)
    }

    private fun hashSessionId(token: String): String {
        return arg2SpringSecurity.encode(token)
    }

    private fun verifySessionId(token: String, hash: String): Boolean {
        return arg2SpringSecurity.matches(token, hash)
    }

    suspend fun verifySession(session: SessionData): Boolean {
        val user = withContext(Dispatchers.IO) {
            // Attempt to find the user tied to the session
            database.gitHubUserQueries.getById(session.userId)
                .executeAsOneOrNull()
        }

        // If they exist, return a session id
        return user != null && verifySessionId(session.sessionId, user.session_id_hash)
    }

    suspend fun getSessionUser(session: SessionData): AuthUserResponse {
        val user = withContext(Dispatchers.IO) {
            // Attempt to find the user tied to the session
            database.gitHubUserQueries.getById(session.userId)
                .executeAsOneOrNull()
                ?: throw NotFoundException()
        }

        return AuthUserResponse(
            id = user.id,
            login = user.login,
            name = user.name,
        )
    }

    suspend fun handleOAuthCallback(
        principal: OAuthAccessTokenResponse.OAuth2,
    ): SessionData {
        val now = OffsetDateTime.now()

        // Retrieve the access tokens & expiry time
        val accessToken = GHUserToken(principal.accessToken)
        val accessTokenExpiry = now.plusSeconds(principal.expiresIn)

        val refreshToken = principal.refreshToken
        val refreshTokenExpiry = principal.extraParameters["refresh_token_expires_in"]
            ?.toLongOrNull()
            ?.let { now.plusSeconds(it) }

        val sessionId = createSessionId()
        val sessionIdHash = hashSessionId(sessionId)

        return withContext(Dispatchers.IO) {
            // Fetch user info from the token
            val me = gitHubClient.getUser(accessToken)

            val newUser = GitHubUser(
                id = me.id,
                login = me.login,
                name = me.name ?: me.login,
                token = accessToken.token,
                token_expiry = accessTokenExpiry,
                refresh_token = refreshToken,
                refresh_token_expiry = refreshTokenExpiry,
                session_id_hash = sessionIdHash
            )

            // Create or update a user record with the new data
            database.gitHubUserQueries.upsertUser(newUser)

            SessionData(
                userId = me.id,
                sessionId = sessionId,
            )
        }
    }

}