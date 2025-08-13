package com.example.config

import io.ktor.server.application.*
import io.ktor.server.config.*

/**
 * Swagger UI機能が有効かどうかを取得
 */
fun ApplicationConfig.isSwaggerEnabled(): Boolean {
    return tryGetString("swagger.enabled")?.toBoolean() ?: true
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
