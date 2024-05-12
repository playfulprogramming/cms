package com.unicornutterances.cms.github.client.data

import kotlinx.serialization.Serializable

@Serializable
data class GHUser(
    val id: Int,
    val login: String,
    val name: String?,
)
