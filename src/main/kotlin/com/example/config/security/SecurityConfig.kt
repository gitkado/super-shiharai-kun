package com.example.config.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config.*
import com.example.domain.auth.model.valueobject.Email
import com.example.util.JwtUtil
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val jwtAudience = environment.config.getJwtAudience()
    val jwtDomain = environment.config.getJwtDomain()
    val jwtRealm = environment.config.getJwtRealm()
    val jwtSecret = environment.config.getJwtSecret()
    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
                    val userId = credential.payload.getClaim(JwtUtil.USER_ID_CLAIM)?.asLong()
                    val email = credential.payload.getClaim(JwtUtil.EMAIL_CLAIM)?.asString()
                    val name = credential.payload.getClaim(JwtUtil.NAME_CLAIM)?.asString()
                    val companyName = credential.payload.getClaim(JwtUtil.COMPANY_NAME_CLAIM)?.asString()

                    if (userId != null && email != null && name != null && companyName != null) {
                        UserPrincipal(userId, Email(email), name, companyName)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }
}
