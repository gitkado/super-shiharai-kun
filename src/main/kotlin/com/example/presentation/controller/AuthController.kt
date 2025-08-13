package com.example.presentation.controller

import com.example.config.getJwtAudience
import com.example.config.getJwtDomain
import com.example.config.getJwtSecret
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.service.AuthService
import com.example.presentation.dto.request.LoginRequest
import com.example.presentation.dto.request.RegisterRequest
import com.example.presentation.dto.response.ErrorResponse
import com.example.presentation.dto.response.LoginResponse
import com.example.presentation.dto.response.ValidationError
import com.example.presentation.dto.response.toRegisterResponse
import com.example.util.JwtUtil
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.request.*
import io.ktor.server.response.*

class AuthController(private val authService: AuthService) {
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
    suspend fun login(
        call: ApplicationCall,
        config: ApplicationConfig,
    ) {
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

        val accessToken =
            JwtUtil.generateToken(
                user,
                config.getJwtSecret(),
                config.getJwtAudience(),
                config.getJwtDomain(),
            )
        val loginResponse =
            LoginResponse(
                accessToken = accessToken,
                expiresIn = JwtUtil.JWT_EXPIRATION_SECONDS,
            )

        call.respond(HttpStatusCode.OK, loginResponse)
    }
}
