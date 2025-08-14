package com.example.domain.payable.service

import com.example.application.port.TransactionRunner
import com.example.domain.payable.model.Invoice
import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate
import com.example.domain.payable.repository.InvoiceRepository
import java.time.LocalDate
import java.time.OffsetDateTime

class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val trx: TransactionRunner,
    private val feeRate: Rate,
    private val taxRate: Rate,
) {
    private val calculator = InvoiceCalculator(feeRate, taxRate)

    suspend fun registerInvoice(
        userId: Long,
        issueDate: LocalDate,
        paymentAmount: Money,
        paymentDueDate: LocalDate,
    ): Invoice =
        trx.required {
            val result = calculator.compute(paymentAmount)
            val invoice =
                Invoice(
                    userId = userId,
                    issueDate = issueDate,
                    paymentAmount = paymentAmount,
                    fee = result.fee,
                    feeRate = feeRate,
                    taxAmount = result.tax,
                    taxRate = taxRate,
                    totalAmount = result.total,
                    paymentDueDate = paymentDueDate,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                )

            invoiceRepository.create(invoice)
        }

    suspend fun getInvoices(
        userId: Long,
        paymentDueFrom: LocalDate? = null,
        paymentDueTo: LocalDate? = null,
    ): List<Invoice> =
        trx.readOnly {
            // ビジネスルール: paymentDueFromはpaymentDueTo以前である必要がある
            if (paymentDueFrom != null && paymentDueTo != null) {
                require(paymentDueFrom <= paymentDueTo) { "paymentDueFrom must be before or equal to paymentDueTo" }
            }

            invoiceRepository.findByUserIdWithDateRange(userId, paymentDueFrom, paymentDueTo)
        }
}
