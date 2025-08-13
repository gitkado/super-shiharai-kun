import com.example.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            environment {
                config =
                    MapApplicationConfig().apply {
                        // DB設定を追加
                        put("postgres.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
                        put("postgres.user", "test")
                        put("postgres.password", "test")
                        put("database.migration_strategy", "none")
                    }
            }
            application {
                module()
            }
            client.get("/v1/auth/register").apply {
                assertEquals(HttpStatusCode.MethodNotAllowed, status)
            }
        }
}
