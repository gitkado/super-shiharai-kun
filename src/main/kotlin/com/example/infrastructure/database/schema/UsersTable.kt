package com.example.infrastructure.database.schema

import com.example.infrastructure.database.schema.customtypes.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.*

object UsersTable : LongIdTable("users") {
    val companyName = varchar("company_name", length = 255)
    val name = varchar("name", length = 255)
    val email = citext("email").uniqueIndex()

    // NOTE: ハッシュ値
    val password = varchar("password", length = 255)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").defaultExpression(CurrentTimestampWithTimeZone)
}
