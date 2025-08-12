package com.example.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.auth.model.User
import java.util.*

/**
 * JWT ユーティリティ
 * アクセストークンの生成を担当
 *
 * TODO: リフレッシュトークンを採用するのがベストだが現状は積み残しとする
 */
object JwtUtil {
    private const val JWT_SECRET = "secret"
    private const val JWT_AUDIENCE = "jwt-audience"
    private const val JWT_DOMAIN = "https://jwt-provider-domain/"
    const val JWT_EXPIRATION_SECONDS = 24 * 60 * 60L // 24 hours in seconds
    private const val JWT_EXPIRATION_MS = JWT_EXPIRATION_SECONDS * 1000L

    fun generateToken(user: User): String {
        return JWT.create()
            .withAudience(JWT_AUDIENCE)
            .withIssuer(JWT_DOMAIN)
            .withSubject((user.id ?: 0).toString())
            .withClaim("email", user.email.value)
            .withClaim("name", user.name)
            .withClaim("companyName", user.companyName)
            .withExpiresAt(Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
            .sign(Algorithm.HMAC256(JWT_SECRET))
    }
}
