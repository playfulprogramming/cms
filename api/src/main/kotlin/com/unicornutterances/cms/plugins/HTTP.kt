package com.unicornutterances.cms.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureHTTP() {
    // Tells browsers not to accept insecure connections for the host if connected over HTTPS.
    install(HSTS) {
        includeSubDomains = true
    }
    install(DefaultHeaders) {
        // `default-src 'self'` helps prevent XSS/injection attacks - only allows resources from the current origin by default
        header("Content-Security-Policy", "default-src 'self'")
        // `DENY` prevents embedding in other websites
        header("X-Frame-Options", "DENY")
        // `nosniff` mitigates [MIME type sniffing](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#mime_sniffing) which can cause security issues.
        header("X-Content-Type-Options", "nosniff")
        // `noopen` is specific to Internet Explorer 8.
        header("X-Download-Options", "noopen")
        // `0` turns off XSS protection in older browsers, which can create XSS vulnerabilities in otherwise safe websites.
        header("X-XSS-Protection", "0")

    }
    // Prevent cross-origin requests to site content
    install(CORS) {
        allowHost("cms.unicorn-utterances.com")
    }
    // Configures JSON serialization
    install(ContentNegotiation) {
        json()
    }
    // Handles any uncaught exceptions in route handlers
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
}
