package com.example.presentation.routing

import com.example.infrastructure.database.schema.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello")) {
                ValidationResult.Invalid("Body text should start with 'Hello'")
            } else {
                ValidationResult.Valid
            }
        }
    }

    // UserServiceをKoinから注入
    val userService by inject<UserService>()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/health") {
            // UserServiceがKoinで正しく注入されているかテスト
            val serviceInfo = "UserService injected via Koin DI: ${userService::class.simpleName}"
            call.respondText(serviceInfo)
        }
    }
}
