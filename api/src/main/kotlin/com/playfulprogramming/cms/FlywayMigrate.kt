package com.playfulprogramming.cms

import org.flywaydb.core.Flyway
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object FlywayMigrate {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun run() {
        logger.info("Running FlywayMigrate...")

        val koin = startKoin {
            modules(koinModule)
        }

        val flyway = Flyway.configure()
            .locations("migrations")
            .failOnMissingLocations(true)
            .dataSource(koin.koin.get<DataSource>())
            .load()

        flyway.migrate()
    }

}
