package com.playfulprogramming.cms.config

interface EnvConfig {
    val clientUrl: String

    val flyApiUrl: String?
    val flyApiToken: String
    val flyWorkerAppName: String

    val s3PublicUrl: String
    val s3Bucket: String

    val postgresUrl: String
    val postgresUser: String
    val postgresPassword: String
}

class EnvConfigImpl : EnvConfig {
    override val clientUrl: String = System.getenv("CLIENT_URL")

    override val flyApiUrl: String? = System.getenv("FLY_API_URL")
    override val flyApiToken: String = System.getenv("FLY_API_TOKEN")
    override val flyWorkerAppName: String = System.getenv("FLY_WORKER_APP_NAME")

    override val s3PublicUrl: String = System.getenv("S3_PUBLIC_URL")
    override val s3Bucket: String = System.getenv("S3_BUCKET")

    override val postgresUrl: String = System.getenv("POSTGRES_URL")
    override val postgresUser: String = System.getenv("POSTGRES_USER")
    override val postgresPassword: String = System.getenv("POSTGRES_PASSWORD")
}
