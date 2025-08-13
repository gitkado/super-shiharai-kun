package com.example.presentation.dto.request

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class RegisterInvoiceRequest(
    @Contextual val issueDate: LocalDate,
    @Contextual val paymentAmount: BigDecimal,
    @Contextual val paymentDueDate: LocalDate,
)
