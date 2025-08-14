package com.example.config.di

import com.example.application.port.TransactionRunner
import com.example.config.getJwtAudience
import com.example.config.getJwtDomain
import com.example.config.getJwtSecret
import com.example.domain.auth.port.TokenGenerator
import com.example.domain.auth.repository.UserRepository
import com.example.domain.auth.service.AuthService
import com.example.infrastructure.auth.JwtTokenGenerator
import com.example.infrastructure.database.repository.UserRepositoryImpl
import com.example.presentation.controller.AuthController
import io.ktor.server.config.*
import org.koin.dsl.module

/**
 * 認証ドメインモジュール - 認証関連の依存関係
 */
val authModule =
    module {
        single<UserRepository> { UserRepositoryImpl() }
        single<AuthService> { AuthService(get<UserRepository>(), get<TransactionRunner>()) }
        single<AuthController> { AuthController(get<AuthService>(), get<TokenGenerator>()) }

        // TokenGenerator実装（Infrastructure層）
        single<TokenGenerator> {
            val config = get<ApplicationConfig>()
            JwtTokenGenerator(
                jwtSecret = config.getJwtSecret(),
                jwtAudience = config.getJwtAudience(),
                jwtDomain = config.getJwtDomain(),
            )
        }
    }
