package com.example.infrastructure.database

import io.ktor.util.logging.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection

open class Tx(
    private val db: Database,
    private val logger: Logger = KtorSimpleLogger("TX"),
    private val isolation: Int = Connection.TRANSACTION_REPEATABLE_READ,
    private val maxRetries: Int = 2,
) {
    open suspend fun <T> required(block: suspend Transaction.() -> T): T {
        val existing = TransactionManager.currentOrNull()
        return if (existing != null) {
            block(existing) // 既存TXに相乗り
        } else {
            runWithRetry {
                newSuspendedTransaction(Dispatchers.IO, db) {
                    setIsolation(isolation)
                    block(this)
                }
            }
        }
    }

    suspend fun <T> requiresNew(block: suspend Transaction.() -> T): T {
        return runWithRetry {
            newSuspendedTransaction(Dispatchers.IO, db) {
                setIsolation(isolation)
                block(this)
            }
        }
    }

    private suspend fun <T> runWithRetry(exec: suspend () -> T): T {
        var last: Throwable? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return exec()
            } catch (t: Throwable) {
                last = t
                // PostgreSQLの serialization_failure / deadlock 等をここで判別してリトライしてもOK
                if (isRetryableException(t)) {
                    logger.warn("TX attempt ${attempt + 1} failed (retryable): ${t.message}")
                    if (attempt == maxRetries) {
                        logger.error("TX failed after ${maxRetries + 1} attempts")
                    }
                } else {
                    // リトライ不要な例外はそのまま投げる
                    throw t
                }
            }
        }
        throw last!!
    }

    private fun Transaction.setIsolation(level: Int) {
        // Exposedは明示 isolation 変更非推奨だが、必要なら
        connection.transactionIsolation = level
    }

    private fun isRetryableException(t: Throwable): Boolean {
        // PostgreSQLの場合: SQLState 40001 (serialization_failure), 40P01 (deadlock_detected)
        val sqlState =
            when {
                t.cause?.cause is java.sql.SQLException -> (t.cause?.cause as java.sql.SQLException).sqlState
                t.cause is java.sql.SQLException -> (t.cause as java.sql.SQLException).sqlState
                t is java.sql.SQLException -> t.sqlState
                else -> null
            }

        return sqlState in listOf("40001", "40P01") // serialization_failure, deadlock_detected
    }
}
