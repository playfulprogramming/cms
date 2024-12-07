package com.playfulprogramming.cms.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthUserResponse(
    val id: Int,
    val login: String,
    val name: String,
)
