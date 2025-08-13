package com.example.domain.payable.service

import com.example.domain.payable.model.Invoice
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

    @Test
    fun `getInvoices - 全ての請求書を取得できること`() =
        runTest {
            // Given: 複数の請求書を登録
            val userId = 1L
            val invoice1 = createInvoice(userId, LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15))
            val invoice2 = createInvoice(userId, LocalDate.of(2025, 8, 10), LocalDate.of(2025, 9, 10))
            val invoice3 = createInvoice(userId, LocalDate.of(2025, 8, 20), LocalDate.of(2025, 9, 20))

            // When: 期間指定なしで請求書一覧を取得
            val result = invoiceService.getInvoices(userId)

            // Then: 全ての請求書が支払期日順（昇順）で取得される
            assertEquals(3, result.size)
            assertEquals(invoice2.id, result[0].id) // 2025-09-10
            assertEquals(invoice1.id, result[1].id) // 2025-09-15
            assertEquals(invoice3.id, result[2].id) // 2025-09-20
        }

    @Test
    fun `getInvoices - 期間指定で請求書を取得できること`() =
        runTest {
            // Given: 複数の請求書を登録
            val userId = 1L
            createInvoice(userId, LocalDate.of(2025, 8, 10), LocalDate.of(2025, 9, 10))
            val invoice2 = createInvoice(userId, LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15))
            val invoice3 = createInvoice(userId, LocalDate.of(2025, 8, 20), LocalDate.of(2025, 9, 20))
            createInvoice(userId, LocalDate.of(2025, 8, 25), LocalDate.of(2025, 9, 25))

            // When: paymentDueFrom, paymentDueToを指定して請求書一覧を取得
            val result =
                invoiceService.getInvoices(
                    userId = userId,
                    paymentDueFrom = LocalDate.of(2025, 9, 15),
                    paymentDueTo = LocalDate.of(2025, 9, 20),
                )

            // Then: 指定期間内の請求書のみが取得される
            assertEquals(2, result.size)
            assertEquals(invoice2.id, result[0].id) // 2025-09-15
            assertEquals(invoice3.id, result[1].id) // 2025-09-20
        }

    @Test
    fun `getInvoices - 同じ支払期日の場合は起票日順でソートされること`() =
        runTest {
            // Given: 同じ支払期日で起票日が異なる請求書を登録
            val userId = 1L
            val paymentDueDate = LocalDate.of(2025, 9, 15)
            val invoice1 = createInvoice(userId, LocalDate.of(2025, 8, 20), paymentDueDate)
            val invoice2 = createInvoice(userId, LocalDate.of(2025, 8, 10), paymentDueDate)
            val invoice3 = createInvoice(userId, LocalDate.of(2025, 8, 15), paymentDueDate)

            // When: 請求書一覧を取得
            val result = invoiceService.getInvoices(userId)

            // Then: 起票日順（昇順）でソートされる
            assertEquals(3, result.size)
            assertEquals(invoice2.id, result[0].id) // 起票日: 2025-08-10
            assertEquals(invoice3.id, result[1].id) // 起票日: 2025-08-15
            assertEquals(invoice1.id, result[2].id) // 起票日: 2025-08-20
        }

    @Test
    fun `getInvoices - 他のユーザーの請求書は取得されないこと`() =
        runTest {
            // Given: 異なるユーザーの請求書を登録
            val userId1 = 1L
            val userId2 = 2L
            val invoice1 = createInvoice(userId1, LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15))
            createInvoice(userId2, LocalDate.of(2025, 8, 20), LocalDate.of(2025, 9, 20))

            // When: userId1の請求書一覧を取得
            val result = invoiceService.getInvoices(userId1)

            // Then: userId1の請求書のみ取得される
            assertEquals(1, result.size)
            assertEquals(invoice1.id, result[0].id)
        }

    private suspend fun createInvoice(
        userId: Long,
        issueDate: LocalDate,
        paymentDueDate: LocalDate,
    ): Invoice {
        return invoiceService.registerInvoice(
            userId = userId,
            issueDate = issueDate,
            paymentAmount = Money.of(BigDecimal("10000.00")),
            paymentDueDate = paymentDueDate,
        )
    }
}
