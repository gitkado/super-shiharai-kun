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
    const val JWT_EXPIRATION_SECONDS = 24 * 60 * 60L // 24 hours in seconds
    private const val JWT_EXPIRATION_MS = JWT_EXPIRATION_SECONDS * 1000L
    const val USER_ID_CLAIM = "userId"
    const val EMAIL_CLAIM = "email"
    const val NAME_CLAIM = "name"
    const val COMPANY_NAME_CLAIM = "companyName"

    fun generateToken(
        user: User,
        jwtSecret: String,
        jwtAudience: String,
        jwtDomain: String,
    ): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .withSubject((user.id ?: 0).toString())
            .withClaim(USER_ID_CLAIM, user.id ?: 0)
            .withClaim(EMAIL_CLAIM, user.email.value)
            .withClaim(NAME_CLAIM, user.name)
            .withClaim(COMPANY_NAME_CLAIM, user.companyName)
            .withExpiresAt(Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}
