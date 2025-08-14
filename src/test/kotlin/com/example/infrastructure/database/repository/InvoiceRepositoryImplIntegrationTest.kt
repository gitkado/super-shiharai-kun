package com.example.infrastructure.database.repository

import com.example.config.getPostgresPassword
import com.example.config.getPostgresUrl
import com.example.config.getPostgresUser
import com.example.domain.payable.model.Invoice
import com.example.domain.payable.model.valueobject.Money
import com.example.domain.payable.model.valueobject.Rate
import com.example.infrastructure.database.repository.schema.TestUsersTable
import com.example.infrastructure.database.schema.InvoicesTable
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.*

/**
 * InvoiceRepositoryImpl の H2統合テスト
 * PostgreSQLモードのH2でRepository実装のSQLロジックを検証
 * runBlockingを使用してsuspend関数をテスト
 */
class InvoiceRepositoryImplIntegrationTest {
    private lateinit var repository: InvoiceRepositoryImpl

    @BeforeTest
    fun setUp() {
        // テスト設定からH2 PostgreSQLモードでデータベース接続
        val config = ApplicationConfig("application.yaml")
        val database =
            Database.connect(
                url = config.getPostgresUrl(),
                driver = "org.h2.Driver",
                user = config.getPostgresUser(),
                password = config.getPostgresPassword(),
            )

        // テーブル作成（H2用のテストテーブル使用）
        transaction(database) {
            SchemaUtils.create(TestUsersTable, InvoicesTable)
        }

        repository = InvoiceRepositoryImpl()
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(InvoicesTable, TestUsersTable)
        }
    }

    @Test
    fun `findByUserIdWithDateRange - 指定した期間内のユーザの請求書が取得できること`() =
        runBlocking {
            transaction {
                // Given: 複数の請求書データを準備
                val userId = 1L
                setupTestUser(userId)

                val invoice1 = createTestInvoice(userId, LocalDate.of(2025, 8, 10), LocalDate.of(2025, 9, 10))
                val invoice2 = createTestInvoice(userId, LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15))
                val invoice3 = createTestInvoice(userId, LocalDate.of(2025, 8, 20), LocalDate.of(2025, 9, 20))
                val invoice4 = createTestInvoice(userId, LocalDate.of(2025, 8, 25), LocalDate.of(2025, 9, 25))

                runBlocking {
                    repository.create(invoice1)
                    repository.create(invoice2)
                    repository.create(invoice3)
                    repository.create(invoice4)

                    // When: 期間指定でWHERE句による絞り込み
                    val result =
                        repository.findByUserIdWithDateRange(
                            userId = userId,
                            paymentDueFrom = LocalDate.of(2025, 9, 15),
                            paymentDueTo = LocalDate.of(2025, 9, 20),
                        )

                    // Then: 期間内の請求書のみが取得される
                    assertEquals(2, result.size)
                    assertEquals(LocalDate.of(2025, 9, 15), result[0].paymentDueDate)
                    assertEquals(LocalDate.of(2025, 9, 20), result[1].paymentDueDate)
                }
            }
        }

    @Test
    fun `findByUserIdWithDateRange - 支払期日昇順、その後起票日昇順で取得されること`() =
        runBlocking {
            transaction {
                // Given: 同じ支払期日で起票日が異なる請求書
                val userId = 1L
                setupTestUser(userId)

                val paymentDueDate = LocalDate.of(2025, 9, 15)
                val invoice1 = createTestInvoice(userId, LocalDate.of(2025, 8, 20), paymentDueDate)
                val invoice2 = createTestInvoice(userId, LocalDate.of(2025, 8, 10), paymentDueDate)
                val invoice3 = createTestInvoice(userId, LocalDate.of(2025, 8, 15), paymentDueDate)

                runBlocking {
                    repository.create(invoice1)
                    repository.create(invoice2)
                    repository.create(invoice3)

                    // When: ORDER BY の動作確認
                    val result = repository.findByUserIdWithDateRange(userId, null, null)

                    // Then: 支払期日昇順、その後起票日昇順でソートされる
                    assertEquals(3, result.size)
                    assertEquals(LocalDate.of(2025, 8, 10), result[0].issueDate) // 起票日昇順
                    assertEquals(LocalDate.of(2025, 8, 15), result[1].issueDate)
                    assertEquals(LocalDate.of(2025, 8, 20), result[2].issueDate)
                }
            }
        }

    @Test
    fun `findByUserIdWithDateRange - 他ユーザの請求書は取得されないこと`() =
        runBlocking {
            transaction {
                // Given: 異なるユーザーの請求書
                val userId1 = 1L
                val userId2 = 2L
                setupTestUser(userId1)
                setupTestUser(userId2)

                val invoice1 = createTestInvoice(userId1, LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15))
                val invoice2 = createTestInvoice(userId2, LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15))

                runBlocking {
                    repository.create(invoice1)
                    repository.create(invoice2)

                    // When: 特定ユーザーの請求書を取得
                    val result = repository.findByUserIdWithDateRange(userId1, null, null)

                    // Then: 指定ユーザーの請求書のみ取得される
                    assertEquals(1, result.size)
                    assertEquals(userId1, result[0].userId)
                }
            }
        }

    private fun createTestInvoice(
        userId: Long,
        issueDate: LocalDate,
        paymentDueDate: LocalDate,
    ): Invoice {
        return Invoice(
            id = null,
            userId = userId,
            issueDate = issueDate,
            paymentAmount = Money.of(BigDecimal("10000.00")),
            fee = Money.of(BigDecimal("400.00")),
            feeRate = Rate(BigDecimal("0.04")),
            taxAmount = Money.of(BigDecimal("40.00")),
            taxRate = Rate(BigDecimal("0.10")),
            totalAmount = Money.of(BigDecimal("10440.00")),
            paymentDueDate = paymentDueDate,
            createdAt = null,
            updatedAt = null,
        )
    }

    private fun setupTestUser(userId: Long) {
        TestUsersTable.insert {
            it[id] = userId
            it[companyName] = "テスト会社"
            it[name] = "テストユーザー"
            it[email] = "test$userId@example.com"
            it[password] = "hashed_password"
        }
    }
}
