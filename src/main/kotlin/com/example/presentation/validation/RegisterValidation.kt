package com.example.presentation.validation

import com.example.presentation.dto.request.RegisterRequest
import com.example.presentation.dto.response.ValidationError

object RegisterValidation {
    fun validate(request: RegisterRequest): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // Company name validation
        if (request.companyName.isBlank()) {
            errors.add(ValidationError("companyName", "Company name is required"))
        } else if (request.companyName.length > 255) {
            errors.add(ValidationError("companyName", "Company name must be 255 characters or less"))
        }

        // Name validation
        if (request.name.isBlank()) {
            errors.add(ValidationError("name", "Name is required"))
        } else if (request.name.length > 255) {
            errors.add(ValidationError("name", "Name must be 255 characters or less"))
        }

        return errors
    }
}
