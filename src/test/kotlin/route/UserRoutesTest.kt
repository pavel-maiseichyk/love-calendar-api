package route

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import mapper.toUser
import model.core.ApiException.ErrorCode
import model.core.ErrorResponse
import model.user.UserResponse
import model.user.testUserEntity
import model.user.testUserID
import repository.FakeUserRepository
import util.BaseTestWithoutKoin
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserRoutesTest : BaseTestWithoutKoin() {
    lateinit var userRepository: FakeUserRepository

    @BeforeTest
    fun setUp() {
        userRepository = FakeUserRepository()
    }

    @Test
    fun `Get user, success`() = testWithUserRoutes(userRepository) {
        userRepository.users = mutableListOf(testUserEntity)

        val response = client.get("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)

        val userResponse = Json.decodeFromString<UserResponse>(response.bodyAsText())
        assertThat(testUserEntity.toUser()).isDataClassEqualTo(userResponse.user)
    }

    @Test
    fun `Get user, user not found`() = testWithUserRoutes(userRepository) {
        assertThat(userRepository.users.size).isEqualTo(0)

        val response = client.get("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND.name)
        assertThat(errorResponse.message).isEqualTo("User not found.")
    }

    @Test
    fun `Update user, success`() = testWithUserRoutes(userRepository) {
        userRepository.users = mutableListOf(testUserEntity)
        val body = Json.encodeToString(testUserEntity.toUser())

        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(body)
            bearerAuth(token = generateTestJWT(testUserID))
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `Update user, fails due to repository`() = testWithUserRoutes(userRepository) {
        userRepository.shouldSucceed = false
        val body = Json.encodeToString(testUserEntity.toUser())

        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(body)
            bearerAuth(token = generateTestJWT(testUserID))
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Unable to update user.")
    }

    @Test
    fun `Delete user, success`() = testWithUserRoutes(userRepository) {
        userRepository.users = mutableListOf(testUserEntity)

        val response = client.delete("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `Delete user, fails due to repository`() = testWithUserRoutes(userRepository) {
        userRepository.shouldSucceed = false

        val response = client.delete("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Unable to delete user.")
    }
}