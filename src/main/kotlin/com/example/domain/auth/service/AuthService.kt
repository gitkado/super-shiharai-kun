package com.example.domain.auth.service

import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.repository.UserRepository
import com.example.infrastructure.database.Tx

class AuthService(
    private val userRepository: UserRepository,
    private val tx: Tx,
) {
    suspend fun registerUser(
        companyName: String,
        name: String,
        email: Email,
        password: Password,
    ): User =
        tx.required {
            // Create user
            val user =
                User(
                    companyName = companyName,
                    name = name,
                    email = email,
                    password = password.hash(),
                )

            val userId = userRepository.create(user)
            userRepository.findById(userId)!!
        }

    suspend fun authenticateUser(
        email: Email,
        rawPassword: String,
    ): User? {
        val user = userRepository.findByEmail(email) ?: return null

        return if (user.password.verify(rawPassword)) user else null
    }
}
