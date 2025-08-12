package com.example.domain.auth.model

import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import java.time.OffsetDateTime

data class User(
    val id: Long? = null,
    val companyName: String,
    val name: String,
    val email: Email,
    val password: Password,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
) {
    init {
        require(companyName.isNotBlank()) { "companyName must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
    }
}
