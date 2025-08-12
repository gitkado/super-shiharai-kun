package com.example.config.database

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
    val url = environment.config.property("postgres.url").getString()
    val user = environment.config.property("postgres.user").getString()
    val password = environment.config.property("postgres.password").getString()

    log.info("Database configuration: url=$url, user=$user")

    // HikariCP接続プールの設定
    val hikari =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = url
                username = user
                this.password = password
                maximumPoolSize = environment.config.propertyOrNull("postgres.pool.max")?.getString()?.toInt() ?: 10
                minimumIdle = environment.config.propertyOrNull("postgres.pool.min")?.getString()?.toInt() ?: 2
                leakDetectionThreshold = 10_000 // 開発時のリーク検出
                driverClassName = if (url.contains("h2")) "org.h2.Driver" else "org.postgresql.Driver"
            },
        )

    val database = Database.connect(hikari)
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

    log.info("Database connection pool configured: max=${hikari.maximumPoolSize}, min=${hikari.minimumIdle}")

    val strategy =
        runCatching {
            MigrationStrategy.valueOf(
                environment.config.property("database.migration_strategy").getString().uppercase(),
            )
        }.getOrElse {
            log.warn("Invalid migration_strategy, falling back to EXPOSED")
            MigrationStrategy.EXPOSED
        }

    when (strategy) {
        MigrationStrategy.FLYWAY -> {
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
        MigrationStrategy.EXPOSED -> {
            log.warn("Auto-creating tables via Exposed (dev-only, not recommended for production)")
            transaction(database) {
                // 全テーブルを配列で管理
                val tables =
                    arrayOf(
                        UsersTable,
                        // 新しいテーブルはここに追加
                    )
                SchemaUtils.createMissingTablesAndColumns(*tables)
                log.info("Created/updated ${tables.size} tables: ${tables.joinToString { it.tableName }}")
            }
        }
        MigrationStrategy.NONE -> {
            log.info("Skipping database migrations - assuming pre-provisioned DB")
        }
    }

    monitor.subscribe(ApplicationStopping) {
        try {
            hikari.close()
            log.info("Database connection pool closed successfully")
        } catch (e: Exception) {
            log.error("Error closing database connection pool", e)
        }
    }

    // HikariDataSourceをApplication Attributesに格納してKoinで参照できるようにする
    attributes.put(HikariDataSourceKey, hikari)

    log.info("Database configuration completed successfully")
    log.info("Services will be initialized via Koin DI")
}
