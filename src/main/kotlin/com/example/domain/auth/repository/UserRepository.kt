package com.example.domain.auth.repository

import com.example.domain.auth.model.User

interface UserRepository {
    suspend fun create(user: User): Long

    suspend fun findById(id: Long): User?

    suspend fun findByEmail(email: String): User?

    suspend fun update(
        id: Long,
        user: User,
    )

    suspend fun delete(id: Long)
}
