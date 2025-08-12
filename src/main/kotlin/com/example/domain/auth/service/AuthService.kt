package com.example.domain.auth.service

import com.example.domain.auth.model.User
import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.repository.UserRepository

class AuthService(private val userRepository: UserRepository) {
    suspend fun registerUser(
        companyName: String,
        name: String,
        email: Email,
        password: Password,
    ): User {
        // Check if email already exists
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            throw IllegalArgumentException("Email already exists")
        }

        // Create user
        val user =
            User(
                companyName = companyName,
                name = name,
                email = email,
                password = password.hash(),
            )

        val userId = userRepository.create(user)
        return userRepository.findById(userId)!!
    }

    suspend fun authenticateUser(
        email: Email,
        rawPassword: String,
    ): User? {
        val user = userRepository.findByEmail(email) ?: return null

        return if (user.password.verify(rawPassword)) user else null
    }
}
