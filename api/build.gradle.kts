plugins {
    kotlin("jvm") version "1.9.23"
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.flyway)
}

group = "com.unicornutterances"
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

// Read values from the root .env file
val env = file("../.env")
    .readLines()
    .filter { it.contains('=') && !it.startsWith('#') }
    .associate {
        val (key, value) = it.split('=')
        key to value.removePrefix("\"").removeSuffix("\"")
    }

tasks.run.configure {
    environment.putAll(env)
}

val flywayMigrationDir = layout.buildDirectory.dir("resources/main/migrations")

sqldelight {
    databases {
        create("Database") {
            packageName.set("$group.cms.sql")
            dialect(libs.sqldelight.postgres.get().toString())
            deriveSchemaFromMigrations.set(true)
            migrationOutputDirectory = flywayMigrationDir
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.postgres.get().toString())
        classpath(libs.flyway.postgres.get().toString())
    }
}

flyway {
    url = "jdbc:${env["POSTGRES_URL"]}"
    user = env["POSTGRES_USER"]
    password = env["POSTGRES_PASSWORD"]
    locations = arrayOf("filesystem:${flywayMigrationDir.get()}")
}

tasks.flywayMigrate.configure {
    // SQLDelight needs to actually generate the migration files
    // before Flyway can use them
    dependsOn("generateMainDatabaseMigrations")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.ktor.server.tests)

    implementation(libs.bundles.ktor)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.logback)
    implementation(libs.hikari)
    implementation(libs.postgres)
    implementation(libs.sqldelight.jdbc)
    implementation(libs.bcprov)
    implementation(libs.spring.security.crypto)
}
