package com.playfulprogramming.cms.config

interface EnvConfig {
    val apiUrl: String
    val clientUrl: String

    val githubClientId: String
    val githubClientSecret: String

    val secretEncryptKey: String
    val secretSignKey: String

    val postgresUrl: String
    val postgresUser: String
    val postgresPassword: String
}

class EnvConfigImpl : EnvConfig {
    override val apiUrl: String = System.getenv("VITE_API_URL")
    override val clientUrl: String = System.getenv("VITE_CLIENT_URL")

    override val githubClientId: String = System.getenv("GITHUB_CLIENT_ID")
    override val githubClientSecret: String = System.getenv("GITHUB_CLIENT_SECRET")

    override val secretEncryptKey: String = System.getenv("SECRET_ENCRYPT_KEY")
    override val secretSignKey: String = System.getenv("SECRET_SIGN_KEY")

    override val postgresUrl: String = System.getenv("POSTGRES_URL")
    override val postgresUser: String = System.getenv("POSTGRES_USER")
    override val postgresPassword: String = System.getenv("POSTGRES_PASSWORD")
}
