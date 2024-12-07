package com.playfulprogramming.cms.tasks.data

import kotlinx.serialization.Serializable

@Serializable
data class PostImageRequest(
    val slug: String
)
