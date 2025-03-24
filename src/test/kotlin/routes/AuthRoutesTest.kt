package com.example.routes

import com.example.utils.BaseRoutesTest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import models.AuthRequest
import models.ErrorResponse
import models.SuccessResponse
import models.UserEntity
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import repository.UserRepository
import security.hashing.HashingService
import security.hashing.SaltedHash
import security.token.TokenConfig
import security.token.TokenService
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRoutesTest : BaseRoutesTest() {
    private val mockRepository = mockk<UserRepository>()
    private val mockHashingService = mockk<HashingService>()
    private val mockTokenService = mockk<TokenService>()
    private val mockTokenConfig = mockk<TokenConfig>()

    private val userID = "67d94220746423125fc46c45"
    private val email = "test@test.com"
    private val password = "password"
    private val salt = "salt"
    private val hash = "hash"
    private val saltedHash = SaltedHash(
        hash = hash,
        salt = salt
    )
    private val authRequest = AuthRequest(
        email = email,
        password = password
    )
    private val userEntity = UserEntity(
        id = userID,
        name = "",
        email = email,
        specialDate = "",
        meetings = emptyList(),
        password = hash,
        salt = salt
    )

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
    fun `POST sign up - Success`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->
        every { runBlocking { mockRepository.getUserEntityByEmail(any()) } } returns null
        every { runBlocking { mockHashingService.generateSaltedHash(any()) } } returns saltedHash
        every { runBlocking { mockRepository.addUserEntity(any()) } } returns true

        val response = client.post("/sign_up") {
            bearerAuth(generateTestToken(userID = userID))
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST sign up - Failure, fields are blank`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->
        val response = client.post("/sign_up") {
            bearerAuth(generateTestToken(userID = userID))
            contentType(ContentType.Application.Json)
            setBody(authRequest.copy(email = ""))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("The fields were filled incorrectly.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST sign up - Failure, user exists`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->

        every { runBlocking { mockRepository.getUserEntityByEmail(any()) } } returns userEntity

        val response = client.post("/sign_up") {
            bearerAuth(generateTestToken(userID = userID))
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("User with email $email already exists.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST sign up - Failure, failed to add user`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->

        every { runBlocking { mockRepository.getUserEntityByEmail(any()) } } returns null
        every { runBlocking { mockHashingService.generateSaltedHash(any()) } } returns saltedHash
        every { runBlocking { mockRepository.addUserEntity(any()) } } returns false

        val response = client.post("/sign_up") {
            bearerAuth(generateTestToken(userID = userID))
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals("Failed to add user.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST sign in - Success`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->

        val token = generateTestToken(userID)
        every { runBlocking { mockRepository.getUserEntityByEmail(any()) } } returns userEntity
        every { runBlocking { mockHashingService.verify(any(), any()) } } returns true
        every { runBlocking { mockTokenService.generate(any(), any()) } } returns token

        val response = client.post("/sign_in") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST sign in - Failure, user not found`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->

        val token = generateTestToken(userID)
        every { runBlocking { mockRepository.getUserEntityByEmail(any()) } } returns null

        val response = client.post("/sign_in") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("User with email $email not found.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST sign in - Failure, invalid password`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->

        val token = generateTestToken(userID)
        every { runBlocking { mockRepository.getUserEntityByEmail(any()) } } returns userEntity
        every { runBlocking { mockHashingService.verify(any(), any()) } } returns false

        val response = client.post("/sign_in") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("Invalid password.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `GET unauthorized - Success`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->
        val response = client.get("/unauthorized")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("Not authorized.", response.body<ErrorResponse>().message)
    }

    @Test
    fun `GET authenticate - Success`() = testWithAuthRoutes(
        hashingService = mockHashingService,
        tokenService = mockTokenService,
        tokenConfig = mockTokenConfig,
        userRepository = mockRepository
    ) { client ->
        val response = client.get("/authenticate") {
            bearerAuth(generateTestToken(userID))
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(true, response.body<SuccessResponse>().success)
    }
}