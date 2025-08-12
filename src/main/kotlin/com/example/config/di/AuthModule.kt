package com.example.config.di

import com.example.domain.auth.repository.UserRepository
import com.example.infrastructure.database.repository.UserRepositoryImpl
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

/**
 * 認証ドメインモジュール - 認証関連の依存関係
 */
val authModule =
    module {
        single<UserRepository> { UserRepositoryImpl(get<Database>()) }
    }
