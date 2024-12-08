package com.playfulprogramming.cms

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.playfulprogramming.cms.config.EnvConfig
import com.playfulprogramming.cms.config.EnvConfigImpl
import com.playfulprogramming.cms.plugins.configureHTTP
import com.playfulprogramming.cms.plugins.configureMonitoring
import com.playfulprogramming.cms.sql.Database
import com.playfulprogramming.cms.tasks.tasksModule
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import javax.sql.DataSource

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::configureApplication)
        .start(wait = true)
}

val koinModule = module {
    singleOf(::EnvConfigImpl) bind EnvConfig::class

    single {
        val envConfig = get<EnvConfig>()
        val dataSource = HikariDataSource()
        dataSource.jdbcUrl = "jdbc:" + envConfig.postgresUrl
        dataSource.username = envConfig.postgresUser
        dataSource.password = envConfig.postgresPassword
        dataSource
    } bind DataSource::class
    single { Database(get<DataSource>().asJdbcDriver()) }
}

fun Application.configureApplication() {
    install(Koin) {
        modules(
            koinModule,
            tasksModule,
            module {
                single { this@configureApplication }
            },
        )
    }

    configureHTTP()
    configureMonitoring()
}
