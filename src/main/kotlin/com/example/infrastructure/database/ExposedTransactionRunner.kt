package com.example.infrastructure.database

import com.example.application.port.TransactionRunner

/**
 * Exposedを使用したTransactionRunnerの実装
 *
 * Domain層のTransactionRunnerポートを実装し、
 * 実際のトランザクション制御をTxクラスに委譲する。
 */
class ExposedTransactionRunner(
    private val tx: Tx,
) : TransactionRunner {
    override suspend fun <T> required(block: suspend () -> T): T {
        return tx.required { block() }
    }

    override suspend fun <T> readOnly(block: suspend () -> T): T {
        return tx.readOnly { block() }
    }

    override suspend fun <T> requiresNew(block: suspend () -> T): T {
        return tx.requiresNew { block() }
    }
}
