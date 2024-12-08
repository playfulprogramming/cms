package com.playfulprogramming.cms

import org.flywaydb.core.Flyway
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object FlywayMigrate {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Executes as a Fly.io release command to migrate the prod database
     * to newer versions on deployment.
     *
     * Runs with:
     * java -cp /app/server.jar com.playfulprogramming.cms.FlywayMigrate
     */
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Running FlywayMigrate...")

        val koin = startKoin {
            modules(koinModule)
        }

        val flyway = Flyway.configure()
            .dataSource(koin.koin.get<DataSource>())
            .load()

        flyway.migrate()
    }

}
