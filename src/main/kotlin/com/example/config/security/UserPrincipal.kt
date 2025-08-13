package com.example.config.security

import com.example.domain.auth.model.valueobject.Email

data class UserPrincipal(
    val userId: Long,
    val email: Email,
    val name: String,
    val companyName: String,
)
