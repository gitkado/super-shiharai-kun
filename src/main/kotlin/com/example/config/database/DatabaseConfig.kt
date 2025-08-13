package com.example.config.database

import com.example.config.getDatabaseMigrationStrategy
import com.example.config.getPostgresPassword
import com.example.config.getPostgresUrl
import com.example.config.getPostgresUser
import com.example.infrastructure.database.schema.InvoicesTable
import com.example.infrastructure.database.schema.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationStopping
import io.ktor.util.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

enum class MigrationStrategy { FLYWAY, EXPOSED, NONE }

// Application Attributeキー定義
val HikariDataSourceKey = AttributeKey<HikariDataSource>("HikariDataSource")

fun Application.configureDatabases() {
    val hikari = createDataSource()
    val database = setupDatabase(hikari)
    runMigrations(hikari, database)
    setupShutdownHook(hikari)
    registerDataSource(hikari)

    log.info("Database configuration completed successfully")
    log.info("Services will be initialized via Koin DI")
}

private fun Application.createDataSource(): HikariDataSource {
    val url = environment.config.getPostgresUrl()
    val user = environment.config.getPostgresUser()
    val password = environment.config.getPostgresPassword()

    log.info("Database configuration: url=$url, user=$user")

    return HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            maximumPoolSize = environment.config.propertyOrNull("postgres.pool.max")?.getString()?.toInt() ?: 10
            minimumIdle = environment.config.propertyOrNull("postgres.pool.min")?.getString()?.toInt() ?: 2
            leakDetectionThreshold = 10_000 // 開発時のリーク検出
            driverClassName = if (url.contains("h2")) "org.h2.Driver" else "org.postgresql.Driver"
        },
    ).also {
        log.info("Database connection pool configured: max=${it.maximumPoolSize}, min=${it.minimumIdle}")
    }
}

private fun setupDatabase(hikari: HikariDataSource): Database {
    val database = Database.connect(hikari)
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
    return database
}

private fun Application.runMigrations(
    hikari: HikariDataSource,
    database: Database,
) {
    val strategy = getMigrationStrategy()

    when (strategy) {
        MigrationStrategy.FLYWAY -> runFlywayMigrations(hikari)
        MigrationStrategy.EXPOSED -> runExposedMigrations(database)
        MigrationStrategy.NONE -> {
            log.info("Skipping database migrations - assuming pre-provisioned DB")
        }
    }
}

private fun Application.getMigrationStrategy(): MigrationStrategy {
    return runCatching {
        MigrationStrategy.valueOf(
            environment.config.getDatabaseMigrationStrategy().uppercase(),
        )
    }.getOrElse {
        log.warn("Invalid migration_strategy, falling back to EXPOSED")
        MigrationStrategy.EXPOSED
    }
}

private fun Application.runFlywayMigrations(hikari: HikariDataSource) {
    log.info("Running Flyway migrations")
    val flyway =
        Flyway.configure()
            .dataSource(hikari)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("1")
            .validateOnMigrate(true)
            .outOfOrder(false)
            .cleanDisabled(true)
            .load()

    try {
        flyway.migrate()
        log.info("Flyway migrations completed successfully")
    } catch (e: Exception) {
        log.error("Flyway migration failed", e)
        throw e
    }
}

private fun Application.runExposedMigrations(database: Database) {
    log.warn("Auto-creating tables via Exposed (dev-only, not recommended for production)")
    transaction(database) {
        val tables =
            listOf(
                UsersTable,
                InvoicesTable,
                // 新しいテーブルはここに追加
            )
        SchemaUtils.createMissingTablesAndColumns(tables = tables.toTypedArray())
        log.info("Created/updated ${tables.size} tables: ${tables.joinToString { it.tableName }}")
    }
}

private fun Application.setupShutdownHook(hikari: HikariDataSource) {
    monitor.subscribe(ApplicationStopping) {
        try {
            hikari.close()
            log.info("Database connection pool closed successfully")
        } catch (e: Exception) {
            log.error("Error closing database connection pool", e)
        }
    }
}

private fun Application.registerDataSource(hikari: HikariDataSource) {
    // HikariDataSourceをApplication Attributesに格納してKoinで参照できるようにする
    attributes.put(HikariDataSourceKey, hikari)
}
