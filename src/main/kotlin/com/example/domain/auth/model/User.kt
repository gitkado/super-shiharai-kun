package com.example.domain.auth.model

import java.time.OffsetDateTime

data class User(
    val id: Long? = null,
    val companyName: String,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
) {
    init {
        require(companyName.isNotBlank()) { "companyName must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(email.isNotBlank()) { "email must not be blank" }
        require(password.isNotBlank()) { "password must not be blank" }
    }
}
