package com.example.domain.payable.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class Invoice(
    val id: Long? = null,
    val userId: Long,
    val issueDate: LocalDate,
    val paymentAmount: BigDecimal,
    val fee: BigDecimal,
    val feeRate: BigDecimal,
    val taxAmount: BigDecimal,
    val taxRate: BigDecimal,
    val totalAmount: BigDecimal,
    val paymentDueDate: LocalDate,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
) {
    init {
        require(paymentAmount > BigDecimal.ZERO) { "paymentAmount must be positive" }
        require(fee >= BigDecimal.ZERO) { "fee must not be negative" }
        require(feeRate >= BigDecimal.ZERO) { "feeRate must not be negative" }
        require(taxAmount >= BigDecimal.ZERO) { "taxAmount must not be negative" }
        require(taxRate >= BigDecimal.ZERO) { "taxRate must not be negative" }
        require(totalAmount > BigDecimal.ZERO) { "totalAmount must be positive" }
        require(paymentDueDate >= issueDate) { "paymentDueDate must not be before issueDate" }
    }
}
