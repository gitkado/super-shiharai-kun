package com.example.domain.auth.service.fixture

import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.repository.UserRepository
import java.time.OffsetDateTime

/**
 * テスト用のインメモリUserRepository実装
 * データベースに依存しないテストを可能にする
 */
class FakeUserRepository : UserRepository {
    private val store = mutableMapOf<Long, User>()
    private var sequence = 0L

    override suspend fun create(user: User): Long {
        val id = ++sequence
        val now = OffsetDateTime.now()
        val userWithId =
            user.copy(
                id = id,
                createdAt = now,
                updatedAt = now,
            )
        store[id] = userWithId
        return id
    }

    override suspend fun findById(id: Long): User? = store[id]

    override suspend fun findByEmail(email: Email): User? = store.values.firstOrNull { it.email == email }

    override suspend fun update(
        id: Long,
        user: User,
    ) {
        val existing = store[id]
        if (existing != null) {
            store[id] =
                user.copy(
                    id = id,
                    createdAt = existing.createdAt,
                    updatedAt = OffsetDateTime.now(),
                )
        }
    }

    override suspend fun delete(id: Long) {
        store.remove(id)
    }

    // テスト用のヘルパーメソッド
    fun clear() {
        store.clear()
        sequence = 0L
    }

    fun size(): Int = store.size

    fun findAll(): List<User> = store.values.toList()
}
