package com.example.domain.payable.model.valueobject

import java.math.RoundingMode

/**
 * 金額計算に関する共通ポリシー
 *
 * システム全体での金額の精度と丸め規約を統一管理
 */
object MonetaryPolicy {
    /** 金額の小数点以下の桁数 */
    const val SCALE: Int = 2

    /** 丸め規約（四捨五入） */
    val ROUNDING_MODE: RoundingMode = RoundingMode.HALF_UP
}
