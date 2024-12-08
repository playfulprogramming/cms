package com.playfulprogramming.cms.backends.flyio.data

import kotlinx.serialization.Serializable

@Serializable
data class FlyMachine(
    val id: String,
    val name: String,
    /**
     * See: [Machine States](https://fly.io/docs/machines/machine-states/)
     */
    val state: String,
)
