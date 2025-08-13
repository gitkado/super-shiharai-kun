package com.example.infrastructure.database.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object InvoicesTable : LongIdTable("invoices") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val issueDate = date("issue_date")
    val paymentAmount = decimal("payment_amount", 15, 2)
    val fee = decimal("fee", 15, 2)
    val feeRate = decimal("fee_rate", 5, 4) // 手数料率（例: 0.04 = 4%）
    val taxAmount = decimal("tax_amount", 15, 2)
    val taxRate = decimal("tax_rate", 5, 4) // 消費税率（例: 0.10 = 10%）
    val totalAmount = decimal("total_amount", 15, 2)
    val paymentDueDate = date("payment_due_date")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}
