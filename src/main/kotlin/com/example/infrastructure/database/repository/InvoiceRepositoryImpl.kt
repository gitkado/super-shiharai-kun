package com.example.infrastructure.database.repository

import com.example.domain.payable.model.Invoice
import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate
import com.example.domain.payable.repository.InvoiceRepository
import com.example.infrastructure.database.schema.InvoicesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import java.time.OffsetDateTime

class InvoiceRepositoryImpl(private val database: Database) : InvoiceRepository {
    override suspend fun create(invoice: Invoice): Invoice =
        dbQuery {
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

            invoice.copy(id = insertedId, createdAt = now, updatedAt = now)
        }

    override suspend fun findById(id: Long): Invoice? =
        dbQuery {
            InvoicesTable.selectAll()
                .where { InvoicesTable.id.eq(id) }
                .map { it.toInvoice() }
                .singleOrNull()
        }

    override suspend fun findByUserId(userId: Long): List<Invoice> =
        dbQuery {
            InvoicesTable.selectAll()
                .where { InvoicesTable.userId.eq(userId) }
                .map { it.toInvoice() }
        }

    override suspend fun findByUserIdWithDateRange(
        userId: Long,
        paymentDueFrom: LocalDate?,
        paymentDueTo: LocalDate?,
    ): List<Invoice> =
        dbQuery {
            var query =
                InvoicesTable.selectAll()
                    .where { InvoicesTable.userId.eq(userId) }

            paymentDueFrom?.let {
                query = query.andWhere { InvoicesTable.paymentDueDate.greaterEq(it) }
            }

            paymentDueTo?.let {
                query = query.andWhere { InvoicesTable.paymentDueDate.lessEq(it) }
            }

            query.orderBy(InvoicesTable.paymentDueDate to SortOrder.ASC, InvoicesTable.issueDate to SortOrder.ASC)
                .map { it.toInvoice() }
        }

    override suspend fun update(invoice: Invoice): Invoice? =
        dbQuery {
            val id = invoice.id ?: return@dbQuery null
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

            if (updateCount > 0) {
                findById(id)
            } else {
                null
            }
        }

    override suspend fun delete(id: Long): Boolean =
        dbQuery {
            val deletedRows = InvoicesTable.deleteWhere { InvoicesTable.id.eq(id) }
            deletedRows > 0
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

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) { block() }
}
