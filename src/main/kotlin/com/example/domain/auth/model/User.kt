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
        require(companyName.isNotBlank()) { "Company name is required" }
        require(name.isNotBlank()) { "Name is required" }
        require(companyName.length <= 255) { "Company name must be 255 characters or less" }
        require(name.length <= 255) { "Name must be 255 characters or less" }
    }
}
