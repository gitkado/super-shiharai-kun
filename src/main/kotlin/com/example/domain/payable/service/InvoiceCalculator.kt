package com.example.domain.payable.service

import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate

/**
 * 請求書の金額計算を行うクラス
 *
 * 手数料・税金・合計金額の計算ロジックを集約
 */
class InvoiceCalculator(
    private val feeRate: Rate,
    private val taxRate: Rate,
    private val taxBase: (base: Money, fee: Money) -> Money = { _, fee -> fee },
) {
    /**
     * 計算結果を格納するデータクラス
     */
    data class Result(
        val fee: Money,
        val tax: Money,
        val total: Money,
    )

    /**
     * 基準金額から手数料・税金・合計金額を計算
     *
     * @param base 基準となる金額（支払い金額）
     * @return 計算結果
     */
    fun compute(base: Money): Result {
        val fee = base.multiply(feeRate)
        val taxable = taxBase(base, fee)
        val tax = taxable.multiply(taxRate)
        val total = base + fee + tax

        return Result(fee, tax, total)
    }
}
