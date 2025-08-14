package com.example.infrastructure.database.repository

import com.example.domain.auth.exception.UserAlreadyExistsException
import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.repository.UserRepository
import com.example.infrastructure.database.schema.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.SQLException
import java.time.OffsetDateTime

class UserRepositoryImpl : UserRepository {
    override suspend fun create(user: User): Long {
        requireActiveTransaction()
        try {
            val now = OffsetDateTime.now()
            return UsersTable.insert {
                it[companyName] = user.companyName
                it[name] = user.name
                it[email] = user.email.value
                it[password] = user.password.value
                it[createdAt] = now
                it[updatedAt] = now
            }[UsersTable.id].value
        } catch (e: Exception) {
            if (isUniqueViolation(e)) {
                throw UserAlreadyExistsException("User with email ${user.email.value} already exists")
            }
            throw e
        }
    }

    override suspend fun findById(id: Long): User? {
        requireActiveTransaction()
        return UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: Email): User? {
        requireActiveTransaction()
        return UsersTable.selectAll()
            .where { UsersTable.email eq email.value }
            .map { it.toUser() }
            .singleOrNull()
    }

    override suspend fun update(
        id: Long,
        user: User,
    ) {
        requireActiveTransaction()
        UsersTable.update({ UsersTable.id eq id }) {
            it[companyName] = user.companyName
            it[name] = user.name
            it[email] = user.email.value
            it[password] = user.password.value
            it[updatedAt] = OffsetDateTime.now()
        }
    }

    override suspend fun delete(id: Long) {
        requireActiveTransaction()
        UsersTable.deleteWhere { UsersTable.id.eq(id) }
    }

    override suspend fun existsByEmail(email: Email): Boolean {
        requireActiveTransaction()
        return UsersTable.selectAll()
            .where { UsersTable.email eq email.value }
            .count() > 0
    }

    private fun ResultRow.toUser(): User {
        return User(
            id = this[UsersTable.id].value,
            companyName = this[UsersTable.companyName],
            name = this[UsersTable.name],
            email = Email(this[UsersTable.email]),
            password = Password.fromHashed(this[UsersTable.password]),
            createdAt = this[UsersTable.createdAt],
            updatedAt = this[UsersTable.updatedAt],
        )
    }

    private fun requireActiveTransaction() {
        requireNotNull(
            TransactionManager.currentOrNull(),
        ) { "No active transaction. Repository methods must be called within a transaction." }
    }

    private fun isUniqueViolation(e: Exception): Boolean {
        // PostgreSQLの場合: SQLState 23505 (unique_violation)
        val sqlState =
            when {
                e.cause?.cause is SQLException -> (e.cause?.cause as SQLException).sqlState
                e.cause is SQLException -> (e.cause as SQLException).sqlState
                e is SQLException -> e.sqlState
                else -> null
            }

        return sqlState == "23505" // unique_violation
    }
}
