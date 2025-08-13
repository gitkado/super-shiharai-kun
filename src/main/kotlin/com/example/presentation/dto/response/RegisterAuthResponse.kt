package com.example.presentation.dto.response

import com.example.domain.auth.model.User
import kotlinx.serialization.Serializable

@Serializable
data class RegisterAuthResponse(
    val id: Long,
    val companyName: String,
    val name: String,
    val email: String,
    val createdAt: String,
)

fun User.toRegisterResponse(): RegisterAuthResponse {
    return RegisterAuthResponse(
        id = this.id!!,
        companyName = this.companyName,
        name = this.name,
        email = this.email.value,
        createdAt = this.createdAt!!.toString(),
    )
}
