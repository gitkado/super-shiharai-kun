package com.example.domain.auth.service

import com.example.domain.auth.model.valueobject.Email
import com.example.domain.auth.model.valueobject.Password
import com.example.domain.auth.service.fixture.FakeUserRepository
import com.example.domain.auth.service.fixture.MockTx
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * AuthServiceのテストクラス
 */
class AuthServiceTest {
    private val userRepository = FakeUserRepository()
    private val authService = AuthService(userRepository, MockTx())

    @BeforeTest
    fun setUp() {
        userRepository.clear()
    }

    @Test
    fun `registerUser - サービス利用するための自身のユーザを登録できること`() =
        runTest {
            // Given: 有効なユーザー情報
            val companyName = "テスト株式会社"
            val name = "田中太郎"
            val email = Email("tanaka@test.com")
            val password = Password("SecurePass123!")

            // When: ユーザー登録を実行
            val result =
                authService.registerUser(
                    companyName = companyName,
                    name = name,
                    email = email,
                    password = password,
                )

            // Then: ユーザーが正常に作成され、期待される値が返される
            assertNotNull(result.id)
            assertEquals(companyName, result.companyName)
            assertEquals(name, result.name)
            assertEquals(email, result.email)
            assertNotNull(result.password)
            assertTrue(result.password.value != password.value) // パスワードはハッシュ化されている
            assertNotNull(result.createdAt)
            assertNotNull(result.updatedAt)

            // And: リポジトリにユーザーが保存されていることを確認
            assertEquals(1, userRepository.size())
            val savedUser = userRepository.findById(result.id)
            assertNotNull(savedUser)
            assertEquals(email, savedUser.email)
        }

    @Test
    fun `authenticateUser - 正しい認証情報でログインできること`() =
        runTest {
            // Given: 事前にユーザーを登録
            val companyName = "テスト株式会社"
            val name = "田中太郎"
            val email = Email("tanaka@test.com")
            val rawPassword = "SecurePass123!"
            val password = Password(rawPassword)
            authService.registerUser(
                companyName = companyName,
                name = name,
                email = email,
                password = password,
            )

            // When: 正しい認証情報で認証
            val result = authService.authenticateUser(email, rawPassword)

            // Then: 認証が成功し、ユーザー情報が返される
            assertNotNull(result)
            assertEquals(companyName, result.companyName)
            assertEquals(name, result.name)
            assertEquals(email, result.email)
        }
}
