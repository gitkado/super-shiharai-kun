package com.example.domain.auth.service.fixture

import com.example.application.port.TransactionRunner

/**
 * テスト用のTransactionRunnerモック
 * FakeRepositoryを使用するため実際のDBトランザクションは不要
 * ブロックを直接実行して結果を返す
 */
class MockTransactionRunner : TransactionRunner {
    override suspend fun <T> required(block: suspend () -> T): T {
        // テスト用: ブロックを直接実行
        return block()
    }

    override suspend fun <T> readOnly(block: suspend () -> T): T {
        // テスト用: ブロックを直接実行
        return block()
    }

    override suspend fun <T> requiresNew(block: suspend () -> T): T {
        // テスト用: ブロックを直接実行
        return block()
    }
}
