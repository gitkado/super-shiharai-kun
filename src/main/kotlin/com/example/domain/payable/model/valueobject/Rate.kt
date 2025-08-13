package com.example.domain.payable.model.valueobject

import java.math.BigDecimal

/**
 * 料率を表すValue Object
 *
 * 手数料率や税率などの料率を型安全に管理
 */
@JvmInline
value class Rate(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "Rate must be non-negative: $value" }
        require(value <= BigDecimal.ONE) { "Rate must not exceed 1.0: $value" }
        require(value.scale() <= 4) { "Rate precision too high: $value" }
    }

    companion object {
        /**
         * 文字列から料率を作成
         * 例: of("0.04") -> 0.04
         */
        fun of(str: String): Rate = Rate(BigDecimal(str))
    }
}
