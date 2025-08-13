package com.example.config.http

import com.example.config.isDevelopmentMode
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        if (this@configureHTTP.environment.config.isDevelopmentMode()) {
            anyHost()
        } else {
            // TODO: 本番稼働前にオリジンを指定する
            // allowHost("app.example.com", schemes = listOf("https"))
            anyHost()
        }

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
    }
}
