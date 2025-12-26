import api.response.GeneralResponseBuilder
import database.model.User
import io.restassured.RestAssured.post
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import io.restassured.module.kotlin.extensions.Then
import org.hamcrest.Matchers.equalTo
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserResourceTests {

    private val baseUrl = "https://itmo.ssngn.ru/lab4"

    private val userID = System.currentTimeMillis()

    private var requestBody = mapOf<String, Any>()

    @BeforeTest
    fun setup() {
        val username = "AntonE2E_${userID}"
        val password = "TestPWD"

        requestBody = mapOf(
            "username" to username,
            "password" to password
        )
    }

    @Test
    fun `should register new user successfully`() {

        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(
                requestBody
            )
        } When {
            post("/api/user/register/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
            body("message", equalTo("User registered successfully!"))
        }
    }

    fun `should delete user successfully`() {

        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(
                requestBody
            )
        } When {
            post("/api/user/delete/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
            body("message", equalTo("User deleted successfully!"))
        }
    }
}