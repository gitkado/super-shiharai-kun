package com.example.domain.payable.model

import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate
import java.time.LocalDate
import java.time.OffsetDateTime

data class Invoice(
    val id: Long? = null,
    val userId: Long,
    val issueDate: LocalDate,
    val paymentAmount: Money,
    val fee: Money,
    val feeRate: Rate,
    val taxAmount: Money,
    val taxRate: Rate,
    val totalAmount: Money,
    val paymentDueDate: LocalDate,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
) {
    init {
        require(paymentAmount.isPositive()) { "paymentAmount must be positive" }
        require(fee.isPositive()) { "fee must not be negative" }
        require(taxAmount.isPositive()) { "taxAmount must not be negative" }
        require(totalAmount.isPositive()) { "totalAmount must be positive" }
        require(paymentDueDate >= issueDate) { "paymentDueDate must not be before issueDate" }
    }
}
