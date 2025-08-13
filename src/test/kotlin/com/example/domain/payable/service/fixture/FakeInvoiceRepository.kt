package com.example.domain.payable.service.fixture

import com.example.domain.payable.model.Invoice
import com.example.domain.payable.repository.InvoiceRepository
import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicLong

class FakeInvoiceRepository : InvoiceRepository {
    private val invoices = mutableMapOf<Long, Invoice>()
    private val nextId = AtomicLong(1)

    override suspend fun create(invoice: Invoice): Invoice {
        val id = nextId.getAndIncrement()
        val now = OffsetDateTime.now()
        val savedInvoice =
            invoice.copy(
                id = id,
                createdAt = now,
                updatedAt = now,
            )
        invoices[id] = savedInvoice
        return savedInvoice
    }

    override suspend fun findById(id: Long): Invoice? {
        return invoices[id]
    }

    override suspend fun findByUserId(userId: Long): List<Invoice> {
        return invoices.values.filter { it.userId == userId }
    }

    override suspend fun findByUserIdWithDateRange(
        userId: Long,
        paymentDueFrom: java.time.LocalDate?,
        paymentDueTo: java.time.LocalDate?,
    ): List<Invoice> {
        return invoices.values
            .filter { it.userId == userId }
            .filter { invoice ->
                (paymentDueFrom == null || invoice.paymentDueDate >= paymentDueFrom) &&
                    (paymentDueTo == null || invoice.paymentDueDate <= paymentDueTo)
            }
            .sortedWith(compareBy<Invoice> { it.paymentDueDate }.thenBy { it.issueDate })
    }

    override suspend fun update(invoice: Invoice): Invoice? {
        val id = invoice.id ?: return null
        if (invoices.containsKey(id)) {
            val updatedInvoice = invoice.copy(updatedAt = OffsetDateTime.now())
            invoices[id] = updatedInvoice
            return updatedInvoice
        }
        return null
    }

    override suspend fun delete(id: Long): Boolean {
        return invoices.remove(id) != null
    }

    fun clear() {
        invoices.clear()
        nextId.set(1)
    }

    fun size(): Int = invoices.size
}
