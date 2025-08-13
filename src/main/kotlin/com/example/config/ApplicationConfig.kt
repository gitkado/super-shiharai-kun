package com.example.config

import io.ktor.server.config.*

/**
 * 開発モードかどうかを取得
 */
fun ApplicationConfig.isDevelopmentMode(): Boolean {
    return tryGetString("app.development")?.toBoolean() ?: true
}

/**
 * データベースマイグレーション戦略を取得
 */
fun ApplicationConfig.getDatabaseMigrationStrategy(): String {
    return tryGetString("database.migration_strategy") ?: "exposed"
}

/**
 * PostgreSQL接続URLを取得
 */
fun ApplicationConfig.getPostgresUrl(): String {
    return tryGetString("postgres.url") ?: "jdbc:postgresql://localhost:5432/super_shiharai_kun"
}

/**
 * PostgreSQLユーザー名を取得
 */
fun ApplicationConfig.getPostgresUser(): String {
    return tryGetString("postgres.user") ?: "myuser"
}

/**
 * PostgreSQLパスワードを取得
 */
fun ApplicationConfig.getPostgresPassword(): String {
    return tryGetString("postgres.password") ?: "mypassword"
}
