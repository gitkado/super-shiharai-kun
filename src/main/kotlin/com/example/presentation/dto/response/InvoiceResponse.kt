package com.example.presentation.dto.response

import com.example.domain.payable.model.Invoice
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

@Serializable
data class InvoiceResponse(
    val id: Long,
    @Contextual val issueDate: LocalDate,
    @Contextual val paymentAmount: BigDecimal,
    @Contextual val fee: BigDecimal,
    @Contextual val feeRate: BigDecimal,
    @Contextual val taxAmount: BigDecimal,
    @Contextual val taxRate: BigDecimal,
    @Contextual val totalAmount: BigDecimal,
    @Contextual val paymentDueDate: LocalDate,
    @Contextual val createdAt: OffsetDateTime,
    @Contextual val updatedAt: OffsetDateTime,
)

fun Invoice.toInvoiceResponse(): InvoiceResponse {
    return InvoiceResponse(
        id = this.id!!,
        issueDate = this.issueDate,
        paymentAmount = this.paymentAmount.amount,
        fee = this.fee.amount,
        feeRate = this.feeRate.value,
        taxAmount = this.taxAmount.amount,
        taxRate = this.taxRate.value,
        totalAmount = this.totalAmount.amount,
        paymentDueDate = this.paymentDueDate,
        createdAt = this.createdAt!!,
        updatedAt = this.updatedAt!!,
    )
}
