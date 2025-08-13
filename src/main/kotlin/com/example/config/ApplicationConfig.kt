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

/**
 * JWT ドメインを取得
 */
fun ApplicationConfig.getJwtDomain(): String {
    return tryGetString("jwt.domain") ?: "https://jwt-provider-domain/"
}

/**
 * JWT オーディエンスを取得
 */
fun ApplicationConfig.getJwtAudience(): String {
    return tryGetString("jwt.audience") ?: "jwt-audience"
}

/**
 * JWT レルムを取得
 */
fun ApplicationConfig.getJwtRealm(): String {
    return tryGetString("jwt.realm") ?: "ktor sample app"
}

/**
 * JWT シークレットを取得（環境変数から上書き可能）
 */
fun ApplicationConfig.getJwtSecret(): String {
    return tryGetString("jwt.secret") ?: "secret"
}
