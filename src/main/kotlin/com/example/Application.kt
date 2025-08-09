package com.example

import com.example.config.database.configureDatabases
import com.example.config.http.configureHTTP
import com.example.config.monitoring.configureMonitoring
import com.example.config.security.configureSecurity
import com.example.config.serialization.configureSerialization
import com.example.presentation.routing.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureMonitoring()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
