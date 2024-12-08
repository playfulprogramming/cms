package com.playfulprogramming.cms

import org.flywaydb.core.Flyway
import org.koin.core.context.startKoin
import javax.sql.DataSource

object FlywayMigrate {

    /**
     * Executes as a Fly.io release command to migrate the prod database
     * to newer versions on deployment.
     *
     * Runs with:
     * java -cp /app/server.jar com.playfulprogramming.cms.FlywayMigrateKt
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            modules(koinModule)
        }

        val flyway = Flyway.configure()
            .dataSource(koin.koin.get<DataSource>())
            .load()

        flyway.migrate()
    }

}
