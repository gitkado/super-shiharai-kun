package com.example.domain.auth.model.valueobject

import at.favre.lib.crypto.bcrypt.BCrypt

data class Password private constructor(val value: String, private val isHashed: Boolean = false) {
    // プライマリコンストラクタ - 生パスワード用（バリデーション有り）
    constructor(rawPassword: String) : this(rawPassword, false) {
        require(rawPassword.isNotBlank()) { "Password must not be blank" }
        require(rawPassword.length >= 8) { "Password must be at least 8 characters" }
        require(rawPassword.length <= 128) { "Password must be 128 characters or less" }
        require(isValidPasswordComplexity(rawPassword)) {
            "Password must contain at least 3 of the following: uppercase, lowercase, digits, symbols"
        }
    }

    override fun toString(): String = "Password(***)"

    // パスワードをハッシュ化して新しいPasswordインスタンスを生成
    fun hash(): Password {
        if (isHashed) {
            return this // 既にハッシュ済みの場合はそのまま返す
        }
        val hashedValue = BCrypt.withDefaults().hashToString(12, value.toCharArray())
        return Password(hashedValue, true)
    }

    companion object {
        // ハッシュ済みパスワード用のファクトリメソッド（バリデーションなし）
        fun fromHashed(hashedPassword: String): Password {
            return Password(hashedPassword, true)
        }

        private fun isValidPasswordComplexity(password: String): Boolean {
            val hasUppercase = password.any { it.isUpperCase() }
            val hasLowercase = password.any { it.isLowerCase() }
            val hasDigit = password.any { it.isDigit() }
            val hasSymbol = password.any { !it.isLetterOrDigit() }

            val categories = listOf(hasUppercase, hasLowercase, hasDigit, hasSymbol)
            return categories.count { it } >= 3
        }
    }
}
