package com.example.domain.auth.service.fixture

import com.example.infrastructure.database.Tx
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * テスト用のTxモック
 * FakeRepositoryを使用するため実際のDBトランザクションは不要
 * ブロックを直接実行して結果を返す
 */
class MockTx : Tx(Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")) {
    override suspend fun <T> required(block: suspend Transaction.() -> T): T {
        // テスト用: 実際のトランザクションを作成してブロックを実行
        val testDb = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        return runBlocking {
            transaction(db = testDb) {
                runBlocking { block(this@transaction) }
            }
        }
    }

    override suspend fun <T> readOnly(block: suspend Transaction.() -> T): T {
        // テスト用: 読み取り専用も通常のトランザクションで実行（H2では特別な設定不要）
        val testDb = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        return runBlocking {
            transaction(db = testDb) {
                runBlocking { block(this@transaction) }
            }
        }
    }
}
