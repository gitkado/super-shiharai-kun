package com.example.domain.auth.repository

import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email

interface UserRepository {
    suspend fun create(user: User): Long

    suspend fun findById(id: Long): User?

    suspend fun findByEmail(email: Email): User?

    suspend fun update(
        id: Long,
        user: User,
    )

    suspend fun delete(id: Long)
}
