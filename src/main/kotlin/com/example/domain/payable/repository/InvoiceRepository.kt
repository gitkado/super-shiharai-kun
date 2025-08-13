package com.example.domain.payable.repository

import com.example.domain.payable.model.Invoice

interface InvoiceRepository {
    suspend fun create(invoice: Invoice): Invoice

    suspend fun findById(id: Long): Invoice?

    suspend fun findByUserId(userId: Long): List<Invoice>

    suspend fun findByUserIdWithDateRange(
        userId: Long,
        paymentDueFrom: java.time.LocalDate? = null,
        paymentDueTo: java.time.LocalDate? = null,
    ): List<Invoice>

    suspend fun update(invoice: Invoice): Invoice?

    suspend fun delete(id: Long): Boolean
}
