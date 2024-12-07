package com.playfulprogramming.cms.tasks.data

import kotlinx.serialization.Serializable

@Serializable
data class UrlMetadataResponse(
    val title: String?,
    val icon: String?,
    val banner: String?,
)
