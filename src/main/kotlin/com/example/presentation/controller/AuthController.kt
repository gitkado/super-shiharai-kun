package com.example.presentation.controller

import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.service.AuthService
import com.example.presentation.dto.request.RegisterRequest
import com.example.presentation.dto.response.ErrorResponse
import com.example.presentation.dto.response.ValidationError
import com.example.presentation.dto.response.toRegisterResponse
import com.example.presentation.validation.RegisterValidation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class AuthController(private val authService: AuthService) {
    suspend fun register(call: ApplicationCall) {
        val request = call.receive<RegisterRequest>()

        // バリデーション
        val validationErrors = RegisterValidation.validate(request)
        if (validationErrors.isNotEmpty()) {
            val errorResponse =
                ErrorResponse(
                    errors =
                        validationErrors.map {
                            ValidationError(it.field, it.message)
                        },
                )
            call.respond(HttpStatusCode.BadRequest, errorResponse)
            return
        }
        val email =
            try {
                Email(request.email)
            } catch (e: IllegalArgumentException) {
                val errorResponse =
                    ErrorResponse(
                        errors = listOf(ValidationError("email", e.message ?: "Invalid email")),
                    )
                call.respond(HttpStatusCode.BadRequest, errorResponse)
                return
            }
        val password =
            try {
                Password(request.password)
            } catch (e: IllegalArgumentException) {
                val errorResponse =
                    ErrorResponse(
                        errors = listOf(ValidationError("password", e.message ?: "Invalid password")),
                    )
                call.respond(HttpStatusCode.BadRequest, errorResponse)
                return
            }

        try {
            val user =
                authService.registerUser(
                    companyName = request.companyName,
                    name = request.name,
                    email = email,
                    password = password,
                )

            call.respond(HttpStatusCode.Created, user.toRegisterResponse())
        } catch (e: IllegalArgumentException) {
            // StatusPagesで処理されるのでre-throw
            throw e
        }
    }
}
