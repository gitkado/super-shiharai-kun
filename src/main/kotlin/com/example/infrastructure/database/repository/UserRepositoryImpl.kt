package com.example.infrastructure.database.repository

import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.repository.UserRepository
import com.example.infrastructure.database.schema.UsersTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.OffsetDateTime

class UserRepositoryImpl(private val database: Database) : UserRepository {
    override suspend fun create(user: User): Long =
        dbQuery {
            val now = OffsetDateTime.now()
            UsersTable.insert {
                it[companyName] = user.companyName
                it[name] = user.name
                it[email] = user.email.value
                it[password] = user.password.value
                it[createdAt] = now
                it[updatedAt] = now
            }[UsersTable.id].value
        }

    override suspend fun findById(id: Long): User? =
        dbQuery {
            UsersTable.selectAll()
                .where { UsersTable.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }

    override suspend fun findByEmail(email: Email): User? =
        dbQuery {
            UsersTable.selectAll()
                .where { UsersTable.email eq email.value }
                .map { it.toUser() }
                .singleOrNull()
        }

    override suspend fun update(
        id: Long,
        user: User,
    ) = dbQuery {
        UsersTable.update({ UsersTable.id eq id }) {
            it[companyName] = user.companyName
            it[name] = user.name
            it[email] = user.email.value
            it[password] = user.password.value
            it[updatedAt] = OffsetDateTime.now()
        }
        Unit
    }

    override suspend fun delete(id: Long) =
        dbQuery {
            UsersTable.deleteWhere { UsersTable.id.eq(id) }
            Unit
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

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) { block() }
}
