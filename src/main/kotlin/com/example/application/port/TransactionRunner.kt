package com.example.application.port

/**
 * トランザクション実行を抽象化するアプリケーション層ポート
 *
 * 配置理由:
 * - Domain層: 純粋なビジネスロジックのため、技術的関心事であるトランザクション概念は不適切
 * - Application層: 複数ドメインにまたがるアプリケーション横断的な関心事として適切
 * - ユースケース実行時の技術的制約（トランザクション管理）を抽象化
 */
interface TransactionRunner {
    /**
     * 書き込み可能なトランザクション内でブロックを実行
     * 既存トランザクションがあれば相乗り、なければ新規作成
     */
    suspend fun <T> required(block: suspend () -> T): T

    /**
     * 読み取り専用トランザクション内でブロックを実行
     * パフォーマンス最適化が適用される
     */
    suspend fun <T> readOnly(block: suspend () -> T): T

    /**
     * 新しいトランザクション内でブロックを実行
     * 既存トランザクションがあっても独立した新規トランザクションを作成
     */
    suspend fun <T> requiresNew(block: suspend () -> T): T
}
