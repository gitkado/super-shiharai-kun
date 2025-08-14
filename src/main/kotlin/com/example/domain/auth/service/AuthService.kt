package com.example.domain.auth.service

import com.example.application.port.TransactionRunner
import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.repository.UserRepository

class AuthService(
    private val userRepository: UserRepository,
    private val trx: TransactionRunner,
) {
    suspend fun registerUser(
        companyName: String,
        name: String,
        email: Email,
        password: Password,
    ): User =
        trx.required {
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
    ): User? =
        trx.readOnly {
            val user = userRepository.findByEmail(email) ?: return@readOnly null

            if (user.password.verify(rawPassword)) user else null
        }
}
