package com.example.infrastructure.database.repository

import com.example.domain.payable.model.Invoice
import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate
import com.example.domain.payable.repository.InvoiceRepository
import com.example.infrastructure.database.schema.InvoicesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.LocalDate
import java.time.OffsetDateTime

class InvoiceRepositoryImpl : InvoiceRepository {
    override suspend fun create(invoice: Invoice): Invoice {
        requireActiveTransaction()
        val now = OffsetDateTime.now()
        val insertedId =
            InvoicesTable.insert {
                it[userId] = invoice.userId
                it[issueDate] = invoice.issueDate
                it[paymentAmount] = invoice.paymentAmount.amount
                it[fee] = invoice.fee.amount
                it[feeRate] = invoice.feeRate.value
                it[taxAmount] = invoice.taxAmount.amount
                it[taxRate] = invoice.taxRate.value
                it[totalAmount] = invoice.totalAmount.amount
                it[paymentDueDate] = invoice.paymentDueDate
                it[createdAt] = now
                it[updatedAt] = now
            }[InvoicesTable.id].value

        return invoice.copy(id = insertedId, createdAt = now, updatedAt = now)
    }

    override suspend fun findById(id: Long): Invoice? {
        requireActiveTransaction()
        return InvoicesTable.selectAll()
            .where { InvoicesTable.id.eq(id) }
            .map { it.toInvoice() }
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: Long): List<Invoice> {
        requireActiveTransaction()
        return InvoicesTable.selectAll()
            .where { InvoicesTable.userId.eq(userId) }
            .map { it.toInvoice() }
    }

    override suspend fun findByUserIdWithDateRange(
        userId: Long,
        paymentDueFrom: LocalDate?,
        paymentDueTo: LocalDate?,
    ): List<Invoice> {
        requireActiveTransaction()
        var query =
            InvoicesTable.selectAll()
                .where { InvoicesTable.userId.eq(userId) }

        paymentDueFrom?.let {
            query = query.andWhere { InvoicesTable.paymentDueDate.greaterEq(it) }
        }

        paymentDueTo?.let {
            query = query.andWhere { InvoicesTable.paymentDueDate.lessEq(it) }
        }

        return query.orderBy(InvoicesTable.paymentDueDate to SortOrder.ASC, InvoicesTable.issueDate to SortOrder.ASC)
            .map { it.toInvoice() }
    }

    override suspend fun update(invoice: Invoice): Invoice? {
        requireActiveTransaction()
        val id = invoice.id ?: return null
        val updateCount =
            InvoicesTable.update({ InvoicesTable.id.eq(id) }) {
                it[userId] = invoice.userId
                it[issueDate] = invoice.issueDate
                it[paymentAmount] = invoice.paymentAmount.amount
                it[fee] = invoice.fee.amount
                it[feeRate] = invoice.feeRate.value
                it[taxAmount] = invoice.taxAmount.amount
                it[taxRate] = invoice.taxRate.value
                it[totalAmount] = invoice.totalAmount.amount
                it[paymentDueDate] = invoice.paymentDueDate
                it[updatedAt] = OffsetDateTime.now()
            }

        return if (updateCount > 0) {
            findById(id)
        } else {
            null
        }
    }

    override suspend fun delete(id: Long): Boolean {
        requireActiveTransaction()
        val deletedRows = InvoicesTable.deleteWhere { InvoicesTable.id.eq(id) }
        return deletedRows > 0
    }

    private fun ResultRow.toInvoice(): Invoice {
        return Invoice(
            id = this[InvoicesTable.id].value,
            userId = this[InvoicesTable.userId].value,
            issueDate = this[InvoicesTable.issueDate],
            paymentAmount = Money.of(this[InvoicesTable.paymentAmount]),
            fee = Money.of(this[InvoicesTable.fee]),
            feeRate = Rate(this[InvoicesTable.feeRate]),
            taxAmount = Money.of(this[InvoicesTable.taxAmount]),
            taxRate = Rate(this[InvoicesTable.taxRate]),
            totalAmount = Money.of(this[InvoicesTable.totalAmount]),
            paymentDueDate = this[InvoicesTable.paymentDueDate],
            createdAt = this[InvoicesTable.createdAt],
            updatedAt = this[InvoicesTable.updatedAt],
        )
    }

    private fun requireActiveTransaction() {
        requireNotNull(
            TransactionManager.currentOrNull(),
        ) { "No active transaction. Repository methods must be called within a transaction." }
    }
}
