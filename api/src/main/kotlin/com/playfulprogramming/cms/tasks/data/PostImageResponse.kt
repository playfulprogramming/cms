package com.playfulprogramming.cms.tasks.data

import kotlinx.serialization.Serializable

@Serializable
data class PostImageResponse(
    val bannerImage: String,
    val socialImage: String,
)
