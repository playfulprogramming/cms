import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
}

group = "com.playfulprogramming"
version = "0.0.1"

application {
    mainClass.set("com.playfulprogramming.cms.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

// Read values from the root .env file
val env = file("../.env")
    .takeIf { it.exists() }
    ?.readLines()
    ?.filter { it.contains('=') && !it.startsWith('#') }
    ?.associate {
        val (key, value) = it.split('=')
        key to value.removePrefix("\"").removeSuffix("\"")
    }
    ?: emptyMap()

tasks.run.configure {
    environment.putAll(env)
}

tasks.create("runFlywayMigrate", JavaExec::class) {
    environment.putAll(env)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.playfulprogramming.cms.FlywayMigrate"
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

tasks.processResources.configure {
    dependsOn("generateMainDatabaseMigrations")
}

tasks.named<ShadowJar>("shadowJar") {
    // Needed for flyway migrations
    mergeServiceFiles()
    archiveBaseName.set("${project.name}-all")
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
    implementation(libs.flyway.postgres)
    implementation(libs.sqldelight.jdbc)
    implementation(libs.bcprov)
    implementation(libs.spring.security.crypto)
}
