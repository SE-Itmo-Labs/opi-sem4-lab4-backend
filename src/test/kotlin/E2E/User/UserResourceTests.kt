import api.ProjectHTTPHeaders
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import io.restassured.module.kotlin.extensions.Then
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Order
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserResourceTests {

    companion object {
        private val userID = System.currentTimeMillis()
    }

    private val baseUrl = "https://itmo.ssngn.ru/lab4"

    private var requestBody = mapOf<String, Any>()

    private var token = ""

    constructor() {
        setup()
    }

    private fun setup() {
        val username = "AntonE2E_${userID}"
        val password = "TestPWD"

        requestBody = mapOf(
            "username" to username,
            "password" to password
        )
    }

    @BeforeTest
    fun before() {
        println(requestBody.toString())
    }

    @Test
    fun `should register, auth and delete new user successfully`() {
        // 1. Регистрация
        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/user/register/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }


        val authResponse = Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(requestBody)
        } When {
            post("/api/user/auth/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }

        val token = authResponse.extract().path<String>("token")
        println("TOKEN : " + token)

        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, token)
            body(requestBody)
        } When {
            delete("/api/user/delete/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }
    }
}