package com.example.presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val errors: List<ValidationError>)

@Serializable
data class ValidationError(val field: String, val message: String)
