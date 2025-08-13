package com.example.domain.payable.model.valueobject

import java.math.BigDecimal

/**
 * 金額を表すValue Object
 *
 * 金額の精度管理と基本的な演算を提供
 */
@ConsistentCopyVisibility
data class Money private constructor(val amount: BigDecimal) {
    companion object {
        /**
         * BigDecimalから金額オブジェクトを作成
         * 指定された精度と丸め規約を適用
         */
        fun of(raw: BigDecimal): Money =
            Money(
                raw.setScale(MonetaryPolicy.SCALE, MonetaryPolicy.ROUNDING_MODE),
            )

        /**
         * ゼロ金額を作成
         */
        fun zero(): Money = of(BigDecimal.ZERO)
    }

    /**
     * 金額の加算
     */
    operator fun plus(other: Money): Money = of(amount.add(other.amount))

    /**
     * 料率による乗算
     */
    fun multiply(rate: Rate): Money = of(amount.multiply(rate.value))

    /**
     * 正の金額かどうか判定
     */
    fun isPositive(): Boolean = amount > BigDecimal.ZERO

    /**
     * ゼロ金額かどうか判定
     */
    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0
}
