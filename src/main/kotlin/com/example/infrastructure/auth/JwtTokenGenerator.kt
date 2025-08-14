package com.example.infrastructure.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.auth.port.TokenGenerator
import java.time.Instant
import java.util.*

/**
 * JWT を使用したTokenGeneratorの実装
 */
class JwtTokenGenerator(
    private val jwtSecret: String,
    private val jwtAudience: String,
    private val jwtDomain: String,
) : TokenGenerator {
    override fun generate(
        subject: String,
        claims: Map<String, Any>,
        expiresAt: Instant,
    ): String {
        val jwtBuilder =
            JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtDomain)
                .withSubject(subject)
                .withExpiresAt(Date.from(expiresAt))

        // クレームを追加
        claims.forEach { (key, value) ->
            when (value) {
                is String -> jwtBuilder.withClaim(key, value)
                is Int -> jwtBuilder.withClaim(key, value)
                is Long -> jwtBuilder.withClaim(key, value)
                is Boolean -> jwtBuilder.withClaim(key, value)
                is Date -> jwtBuilder.withClaim(key, value)
                else -> jwtBuilder.withClaim(key, value.toString())
            }
        }

        return jwtBuilder.sign(Algorithm.HMAC256(jwtSecret))
    }
}
