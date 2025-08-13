package com.example.domain.payable.service

import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate
import com.example.domain.payable.service.fixture.FakeInvoiceRepository
import kotlinx.coroutines.test.runTest
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.*

class InvoiceServiceTest {
    private val invoiceRepository = FakeInvoiceRepository()
    private val invoiceService = InvoiceService(invoiceRepository)

    @BeforeTest
    fun setUp() {
        invoiceRepository.clear()
    }

    @Test
    fun `registerInvoice - 請求書データを登録できること`() =
        runTest {
            // Given: 有効な請求書情報
            val userId = 1L
            val issueDate = LocalDate.of(2025, 8, 15)
            val paymentAmount = Money.of(BigDecimal("10000.00"))
            val paymentDueDate = LocalDate.of(2025, 9, 15)

            // When: 請求書登録を実行
            val result =
                invoiceService.registerInvoice(
                    userId = userId,
                    issueDate = issueDate,
                    paymentAmount = paymentAmount,
                    paymentDueDate = paymentDueDate,
                )

            // Then: 請求書が正常に登録され、期待される値が返される
            assertNotNull(result.id)
            assertEquals(userId, result.userId)
            assertEquals(issueDate, result.issueDate)
            assertEquals(paymentAmount, result.paymentAmount)
            assertEquals(paymentDueDate, result.paymentDueDate)

            // And: 手数料と税額が正しく計算されている
            val expectedFee = Money.of(BigDecimal("400.00")) // 10000 * 0.04
            val expectedTax = Money.of(BigDecimal("40.00")) // 400 * 0.10
            val expectedTotal = Money.of(BigDecimal("10440.00")) // 10000 + 400 + 40

            assertEquals(expectedFee, result.fee)
            assertEquals(Rate(BigDecimal("0.04")), result.feeRate)
            assertEquals(expectedTax, result.taxAmount)
            assertEquals(Rate(BigDecimal("0.10")), result.taxRate)
            assertEquals(expectedTotal, result.totalAmount)

            assertNotNull(result.createdAt)
            assertNotNull(result.updatedAt)

            // And: リポジトリに請求書が保存されていることを確認
            assertEquals(1, invoiceRepository.size())
            val savedInvoice = invoiceRepository.findById(result.id)
            assertNotNull(savedInvoice)
            assertEquals(userId, savedInvoice.userId)
        }
}
