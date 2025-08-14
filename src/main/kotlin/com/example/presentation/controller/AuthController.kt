package com.example.presentation.controller

import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.port.TokenGenerator
import com.example.domain.auth.service.AuthService
import com.example.presentation.dto.request.LoginRequest
import com.example.presentation.dto.request.RegisterRequest
import com.example.presentation.dto.response.ErrorResponse
import com.example.presentation.dto.response.LoginResponse
import com.example.presentation.dto.response.ValidationError
import com.example.presentation.dto.response.toRegisterResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.Instant

class AuthController(
    private val authService: AuthService,
    private val tokenGenerator: TokenGenerator,
) {
    companion object {
        const val JWT_EXPIRATION_SECONDS = 24 * 60 * 60L // 24 hours
    }

    suspend fun register(call: ApplicationCall) {
        val request = call.receive<RegisterRequest>()

        val email = Email(request.email)
        val password = Password(request.password)

        val user =
            authService.registerUser(
                companyName = request.companyName,
                name = request.name,
                email = email,
                password = password,
            )

        call.respond(HttpStatusCode.Created, user.toRegisterResponse())
    }

    /**
     * ユーザーログイン API
     *
     * メールアドレスとパスワードで認証し、JWTアクセストークンを発行する
     * アクセストークンの有効期限は24時間
     *
     * @param call ApplicationCall
     */
    suspend fun login(call: ApplicationCall) {
        val request = call.receive<LoginRequest>()

        val email = Email(request.email)
        val user = authService.authenticateUser(email, request.password)
        if (user == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(listOf(ValidationError("credentials", "Invalid email or password"))),
            )
            return
        }

        val claims =
            mapOf(
                TokenGenerator.USER_ID_CLAIM to (user.id ?: 0),
                TokenGenerator.EMAIL_CLAIM to user.email.value,
                TokenGenerator.NAME_CLAIM to user.name,
                TokenGenerator.COMPANY_NAME_CLAIM to user.companyName,
            )

        val expiresAt = Instant.now().plusSeconds(JWT_EXPIRATION_SECONDS)
        val accessToken =
            tokenGenerator.generate(
                subject = (user.id ?: 0).toString(),
                claims = claims,
                expiresAt = expiresAt,
            )
        val loginResponse =
            LoginResponse(
                accessToken = accessToken,
                expiresIn = JWT_EXPIRATION_SECONDS,
            )

        call.respond(HttpStatusCode.OK, loginResponse)
    }
}
