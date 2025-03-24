package com.example.routes

import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.utils.BaseRoutesTest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import models.ErrorResponse
import models.SuccessResponse
import models.User
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import repository.UserRepository
import kotlin.test.*

class UserRoutesTest : BaseRoutesTest() {
    private val mockRepository = mockk<UserRepository>()
    private val userID = "67d94220746423125fc46c45"
    private val user = User(id = userID, name = "User", email = "", specialDate = "", meetings = emptyList())

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { mockRepository }
                }
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `GET user - Success`() = testWithUserRoutes(mockRepository) { client ->
        every { runBlocking { mockRepository.getUserByID(userID = userID) } } returns user

        val response = client.get("/users") { bearerAuth(token = generateTestToken(userID)) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(true, response.body<SuccessResponse>().success)
        assertEquals(user, response.body<SuccessResponse>().user)
    }

    @Test
    fun `GET user - Failure, Missing ID in token`() = testWithUserRoutes(mockRepository) { client ->
        val response = client.get("/users") { bearerAuth(token = generateFailedTestToken(userID)) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(false, response.body<ErrorResponse>().success)
        assertEquals("Missing ID.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `GET user - Failure, User not found`() = testWithUserRoutes(mockRepository) { client ->
        every { runBlocking { mockRepository.getUserByID(userID = userID) } } returns null

        val response = client.get("/users") { bearerAuth(token = generateTestToken(userID)) }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(false, response.body<ErrorResponse>().success)
        assertEquals("User with ID $userID not found.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `PUT user - Success`() = testWithUserRoutes(mockRepository) { client ->
        every { runBlocking { mockRepository.updateUser(updatedUser = user) } } returns true

        val response = client.put("/users") {
            bearerAuth(token = generateTestToken(userID))
            contentType(ContentType.Application.Json)
            setBody(user)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(true, response.body<SuccessResponse>().success)
    }

    @Test
    fun `PUT user - Failure, Unable to update user`() = testWithUserRoutes(mockRepository) { client ->
        every { runBlocking { mockRepository.updateUser(updatedUser = user) } } returns false

        val response = client.put("/users") {
            bearerAuth(token = generateTestToken(userID))
            contentType(ContentType.Application.Json)
            setBody(user)
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(false, response.body<ErrorResponse>().success)
        assertEquals("Unable to update user.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `DELETE user - Success`() = testWithUserRoutes(mockRepository) { client ->
        every { runBlocking { mockRepository.removeUser(userID = userID) } } returns true

        val response = client.delete("/users") { bearerAuth(token = generateTestToken(userID)) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(true, response.body<SuccessResponse>().success)
    }

    @Test
    fun `DELETE user - Failure, Missing ID in token`() = testWithUserRoutes(mockRepository) { client ->
        val response = client.delete("/users") { bearerAuth(token = generateFailedTestToken(userID)) }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(false, response.body<ErrorResponse>().success)
        assertEquals("Missing ID.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `DELETE user - Failure, Unable to delete user`() = testWithUserRoutes(mockRepository) { client ->
        every { runBlocking { mockRepository.removeUser(userID = userID) } } returns false

        val response = client.delete("/users") { bearerAuth(token = generateTestToken(userID)) }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(false, response.body<ErrorResponse>().success)
        assertEquals("Unable to delete user.", response.body<ErrorResponse>().message)
    }
}