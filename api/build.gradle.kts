plugins {
    kotlin("jvm") version "1.9.23"
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
}

group = "com.unicorn-utterances"
version = "0.0.1"

application {
    mainClass.set("com.unicornutterances.cms.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.unicornutterances.cms")
            dialect(libs.sqldelight.postgres.get().toString())
            srcDirs("sqldelight")
            deriveSchemaFromMigrations.set(true)
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.tests)

    implementation(libs.bundles.ktor)
    implementation(libs.logback)
    implementation(libs.sqldelight.jdbc)
}
