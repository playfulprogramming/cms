package com.playfulprogramming.cms.tasks.data

import kotlinx.serialization.Serializable

@Serializable
data class UrlMetadataRequest(
    val url: String,
)
