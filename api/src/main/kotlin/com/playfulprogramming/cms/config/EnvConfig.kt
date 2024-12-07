package com.playfulprogramming.cms.config

interface EnvConfig {
    val postgresUrl: String
    val postgresUser: String
    val postgresPassword: String
}

class EnvConfigImpl : EnvConfig {
    override val postgresUrl: String = System.getenv("POSTGRES_URL")
    override val postgresUser: String = System.getenv("POSTGRES_USER")
    override val postgresPassword: String = System.getenv("POSTGRES_PASSWORD")
}
