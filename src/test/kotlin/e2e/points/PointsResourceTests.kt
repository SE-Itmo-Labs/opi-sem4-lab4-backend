import api.ProjectHTTPHeaders
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PointsResourceTests {

    private val baseUrl = "https://itmo.ssngn.ru/lab4"

    private lateinit var authToken: String
    private lateinit var username: String
    private lateinit var password: String

    private val validPoints = listOf(
        mapOf("x" to -1.0f, "y" to 1.0f, "R" to 1.0f),   // в зоне (II четверть)
        mapOf("x" to 0.3f, "y" to 0.8f, "R" to 2.0f),     // в зоне (I четверть)
        mapOf("x" to -0.5f, "y" to -0.5f, "R" to 2.0f),   // в зоне (III четверть)
        mapOf("x" to 0.0f, "y" to 0.0f, "R" to 1.5f)      // origin — в зоне
    )

    private val invalidPoints = listOf(
        mapOf("x" to 1.0f, "y" to -1.0f, "R" to 1.0f),    // IV четверть → ошибка
        mapOf("x" to 2.0f, "y" to 1.0f, "R" to 1.0f),     // I четверть, x > R/2
        mapOf("x" to -1.0f, "y" to 1.0f, "R" to 3.0f),    // R не из разрешённых
        mapOf("x" to 0.0f, "y" to 6.0f, "R" to 1.0f)      // y > 5 → NumberFormatException в билдере
    )

    @BeforeTest
    fun setupUser() {
        val timestamp = System.currentTimeMillis()
        username = "PointE2E_User_$timestamp"
        password = "SecureTestPass123"

        // Register
        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(mapOf("username" to username, "password" to password))
        } When {
            post("/api/user/register/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }

        // Auth
        val authResponse = Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(mapOf("username" to username, "password" to password))
        } When {
            post("/api/user/auth/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }

        authToken = authResponse.extract().path("token")
        assertNotNull(authToken)
    }

    @AfterTest
    fun cleanup() {
        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
            body(mapOf("username" to username))
        } When {
            delete("/api/user/delete/")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `POST points_save - should save valid point successfully`() {
        val point = validPoints[0]

        val response = Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
            body(point)
        } When {
            post("/api/point/save/")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
            body("message", equalTo("Point has been thrown"))
        }

        val savedPoint = response.extract().path<Map<String, Any>>("points[0]")
        assertEquals(point["x"], savedPoint["x"])
        assertEquals(point["y"], savedPoint["y"])
        assertEquals(point["R"], savedPoint["R"])
        assertNotNull(savedPoint["id"])
    }

    @Test
    fun `POST point_save - should reject invalid R value`() {
        val point = mapOf("x" to 0.0f, "y" to 0.0f, "R" to 3.0f) // R тут фейл

        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
            body(point)
        } When {
            post("/api/point/save/")
        } Then {
            statusCode(400)
            body("status", equalTo("error"))
            body("message", containsString("Invalid radius"))
        }
    }

    @Test
    fun `GET point_my - should return only current user's points`() {
        // Сохраним пару точек
        validPoints.take(2).forEach { point ->
            Given {
                baseUri(baseUrl)
                contentType(ContentType.JSON)
                header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
                body(point)
            } When {
                post("/api/point/save/")
            } Then {
                statusCode(200)
            }
        }

        val response = Given {
            baseUri(baseUrl)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
        } When {
            get("/api/point/my")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
            body("points", equalTo(2))
        }

        val points = response.extract().path<List<Map<String, Any>>>("data")
        points.forEach {
            assertEquals(username, it["username"])
        }
    }

    @Test
    fun `GET point_all - should return all points from all users (if any)`() {
        Given {
            baseUri(baseUrl)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
        } When {
            get("/api/point/all")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }
    }

    @Test
    fun `DELETE _point_{id} - should delete own point successfully`() {
        // Сохраним точку
        val point = validPoints[0]
        val saveResponse = Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
            body(point)
        } When {
            post("/api/point/save/")
        } Then {
            statusCode(200)
        }

        val pointId = saveResponse.extract().path<Long>("data[0].id")

        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken)
        } When {
            delete("/api/point/$pointId")
        } Then {
            statusCode(200)
            body("status", equalTo("ok"))
        }
    }

    @Test
    fun `DELETE point_{id} - should not delete another user's point`() {
        // Создаём второго пользователя и точку от его имени
        val username2 = "AnotherUser_${System.currentTimeMillis()}"
        val password2 = "AnotherPass123"

        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(mapOf("username" to username2, "password" to password2))
        } When {
            post("/api/user/register/")
        }

        val resp = Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            body(mapOf("username" to username2, "password" to password2))
        } When {
            post("/api/user/auth/")
        } Then {
            statusCode(200)
        }

        val token2 = resp.extract().path<String>("token")

        val point = validPoints[0]
        val resp2 = Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, token2)
            body(point)
        } When {
            post("/api/point/save/")
        } Then {

        }

        val pointId = resp2.extract().path<Long>("data[0].id")

        // Пытаемся удалить чужую точку
        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, authToken) // свой токен
        } When {
            delete("/api/point/$pointId")
        } Then {
            statusCode(200)
            body("status", equalTo("error"))
            body("message", containsString("Свои точки только можно удалять"))
        }

        // Удалим второго пользователя вручную (cleanup)
        Given {
            baseUri(baseUrl)
            contentType(ContentType.JSON)
            header(ProjectHTTPHeaders.AUTHORIZATION, token2)
            body(mapOf("username" to username2))
        } When {
            delete("/api/user/delete/")
        }
    }
}