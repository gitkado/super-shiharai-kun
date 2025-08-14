package com.example.config.di

import com.example.config.database.HikariDataSourceKey
import com.example.infrastructure.database.Tx
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.util.logging.*
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

/**
 * Koinデータベースモジュール
 * データベース関連の依存関係を管理
 */
val databaseModule =
    module {
        // HikariDataSourceをApplication Attributesから取得
        single<HikariDataSource> {
            val application = get<Application>()
            application.attributes[HikariDataSourceKey]
        }

        // Database instanceをsingletonとして登録
        single<Database> {
            Database.connect(get<HikariDataSource>())
        }

        // Txユーティリティをsingletonとして登録
        single<Tx> {
            Tx(get<Database>(), KtorSimpleLogger("TX"))
        }
    }
