package com.example.presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val companyName: String,
    val name: String,
    val email: String,
    val password: String,
)
