package com.example.presentation.routing

import com.example.config.isDevelopmentMode
import com.example.domain.auth.exception.UserAlreadyExistsException
import com.example.presentation.controller.AuthController
import com.example.presentation.controller.InvoiceController
import com.example.presentation.dto.response.ErrorResponse
import com.example.presentation.dto.response.ValidationError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        // === 400系エラー ===

        // ユーザー重複エラー
        exception<UserAlreadyExistsException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(listOf(ValidationError("email", "Email already exists"))),
            )
        }

        // ビジネスルール違反・バリデーションエラー
        exception<IllegalArgumentException> { call, cause ->
            val field =
                when {
                    cause.message?.contains("email", ignoreCase = true) == true -> "email"
                    cause.message?.contains("password", ignoreCase = true) == true -> "password"
                    cause.message?.contains("fromDate", ignoreCase = true) == true -> "validation"
                    else -> "validation"
                }
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError(field, cause.message ?: "Invalid input"))),
            )
        }

        // 日付フォーマットエラー
        exception<java.time.format.DateTimeParseException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError("validation", "Invalid date format. Use YYYY-MM-DD"))),
            )
        }

        // 数値フォーマットエラー
        exception<NumberFormatException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError("validation", "Invalid number format"))),
            )
        }

        // JSONパースエラー
        exception<kotlinx.serialization.SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError("json", "Invalid JSON format"))),
            )
        }

        // リクエストボディが不正
        exception<io.ktor.server.plugins.BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(listOf(ValidationError("request", cause.message ?: "Bad request"))),
            )
        }

        // === 404系エラー ===

        // リソースが見つからない
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(listOf(ValidationError("resource", "Resource not found"))),
            )
        }

        // === 500系エラー ===

        // データベース接続エラー
        exception<java.sql.SQLException> { call, cause ->
            call.application.log.error("Database error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(listOf(ValidationError("database", "Database error occurred"))),
            )
        }

        // 汎用的な500エラー（最後に配置）
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception: ${cause.javaClass.simpleName}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(listOf(ValidationError("error", "Internal server error"))),
            )
        }
    }
    val authController by inject<AuthController>()
    val invoiceController by inject<InvoiceController>()

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
                authController.login(call, environment.config)
            }
        }

        authenticate("auth-jwt") {
            route("/v1/payable") {
                post("/invoices") {
                    invoiceController.registerInvoice(call)
                }
                get("/invoices") {
                    invoiceController.getInvoices(call)
                }
            }
        }
    }
}
