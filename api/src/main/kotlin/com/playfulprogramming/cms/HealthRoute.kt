package com.playfulprogramming.cms

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

private class HealthRoute(application: Application) {
    init {
        application.routing {
            get("/health") {
                call.respond(HttpStatusCode.OK, "OK")
            }
        }
    }
}

val healthModule = module {
    singleOf(::HealthRoute) withOptions { createdAtStart() }
}
