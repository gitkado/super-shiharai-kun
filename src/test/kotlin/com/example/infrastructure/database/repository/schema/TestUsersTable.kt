package com.example.infrastructure.database.repository.schema

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * テスト用UsersTable
 * H2でCITEXTが使えないため、VARCHARで代用
 */
object TestUsersTable : LongIdTable("users") {
    val companyName = varchar("company_name", length = 255)
    val name = varchar("name", length = 255)
    val email = varchar("email", length = 255).uniqueIndex() // CITEXTの代わりにVARCHAR

    // NOTE: ハッシュ値
    val password = varchar("password", length = 255)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
