package com.example.config.di

import com.example.domain.auth.repository.UserRepository
import com.example.domain.auth.service.AuthService
import com.example.infrastructure.database.Tx
import com.example.infrastructure.database.repository.UserRepositoryImpl
import com.example.presentation.controller.AuthController
import org.koin.dsl.module

/**
 * 認証ドメインモジュール - 認証関連の依存関係
 */
val authModule =
    module {
        single<UserRepository> { UserRepositoryImpl() }
        single<AuthService> { AuthService(get<UserRepository>(), get<Tx>()) }
        single<AuthController> { AuthController(get<AuthService>()) }
    }
