package com.example.domain.auth.port

import java.time.Instant

/**
 * トークン生成を抽象化するポート
 */
interface TokenGenerator {
    /**
     * アクセストークンを生成
     *
     * @param subject トークンの主体（通常はユーザーID）
     * @param claims トークンに含める追加の情報
     * @param expiresAt トークンの有効期限
     * @return 生成されたトークン
     */
    fun generate(
        subject: String,
        claims: Map<String, Any>,
        expiresAt: Instant,
    ): String

    companion object {
        const val USER_ID_CLAIM = "userId"
        const val EMAIL_CLAIM = "email"
        const val NAME_CLAIM = "name"
        const val COMPANY_NAME_CLAIM = "companyName"
    }
}
