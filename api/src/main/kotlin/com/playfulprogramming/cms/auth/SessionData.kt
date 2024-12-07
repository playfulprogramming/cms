package com.playfulprogramming.cms.auth

import io.ktor.server.auth.*

data class SessionData(
    val userId: Int,
    val sessionId: String,
) : Principal
