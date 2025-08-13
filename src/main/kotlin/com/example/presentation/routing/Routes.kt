package com.example.presentation.routing

import com.example.config.isDevelopmentMode
import com.example.presentation.controller.AuthController
import com.example.presentation.dto.response.ErrorResponse
import com.example.presentation.dto.response.ValidationError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError("general", cause.message ?: "Bad request"))),
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    val authController by inject<AuthController>()

    routing {
        // Swagger UI エンドポイント（設定で有効化されている場合のみ）
        if (environment.config.isDevelopmentMode()) {
            swaggerUI(path = "swagger-ui", swaggerFile = "openapi/documentation.yaml")
        }

        route("/v1/auth") {
            post("/register") {
                authController.register(call)
            }
            post("/login") {
                authController.login(call)
            }
        }
    }
}
