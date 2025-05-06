package route
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import model.auth.*
import model.core.ApiException.ErrorCode
import model.core.ErrorResponse
import model.security.testConfig
import model.user.testEmail
import model.user.testPassword
import model.user.testUserEntity
import repository.FakeRefreshTokenRepository
import repository.FakeUserRepository
import security.hashing.FakeHashingService
import security.token.FakeTokenService
import util.BaseTestWithoutKoin
import kotlin.test.BeforeTest
import kotlin.test.Test

class AuthRoutesTest : BaseTestWithoutKoin() {

    lateinit var hashingService: FakeHashingService
    lateinit var tokenService: FakeTokenService
    lateinit var userRepository: FakeUserRepository
    lateinit var refreshTokenRepository: FakeRefreshTokenRepository

    @BeforeTest
    fun setUp() {
        hashingService = FakeHashingService()
        tokenService = FakeTokenService()
        userRepository = FakeUserRepository()
        refreshTokenRepository = FakeRefreshTokenRepository()
    }

    @Test
    fun `Sign up, success`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val tokenResponseResult = Json.decodeFromString<TokenResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(tokenResponseResult.refreshToken).isEqualTo(testRefreshToken)
        assertThat(tokenResponseResult.accessToken).isEqualTo(testAccessToken)
    }

    @Test
    fun `Sign up, failure, email exists`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userRepository.users = mutableListOf(testUserEntity)
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Conflict)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.USER_ALREADY_EXISTS.name)
        assertThat(errorResponse.message).isEqualTo("User already exists.")
    }

    @Test
    fun `Sign up, failure, wrong email format`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        val body = Json.encodeToString(AuthRequest(email = "blablabla", password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_EMAIL_FORMAT.name)
        assertThat(errorResponse.message).isEqualTo("Email format is invalid.")
    }

    @Test
    fun `Sign up, failure, wrong password length`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = "123"))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_PASSWORD_LENGTH.name)
        assertThat(errorResponse.message).isEqualTo("Password length is invalid.")
    }

    @Test
    fun `Sign up, failure, addUserEntity failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userRepository.shouldSucceed = false
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Failed to add user.")
    }

    @Test
    fun `Sign up, failure, refresh token collision`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Token collision detected.")
    }

    @Test
    fun `Sign up, failure, saveRefreshToken failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.shouldSave = false
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Couldn't save refresh token.")
    }

    @Test
    fun `Sign in, success`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userRepository.users = mutableListOf(testUserEntity)
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_in") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val tokenResponseResult = Json.decodeFromString<TokenResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(tokenResponseResult.refreshToken).isEqualTo(testRefreshToken)
        assertThat(tokenResponseResult.accessToken).isEqualTo(testAccessToken)
    }

    @Test
    fun `Sign in, failure, user not found`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        assertThat(userRepository.users.size).isEqualTo(0)
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_in") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val tokenResponseResult = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(tokenResponseResult.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND.name)
        assertThat(tokenResponseResult.message).isEqualTo("User not found.")
    }

    @Test
    fun `Sign in, failure, invalid password`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userRepository.users = mutableListOf(testUserEntity)
        hashingService.shouldSucceed = false
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_in") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val tokenResponseResult = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(tokenResponseResult.errorCode).isEqualTo(ErrorCode.INVALID_PASSWORD.name)
        assertThat(tokenResponseResult.message).isEqualTo("Invalid password.")
    }

    @Test
    fun `Sign in, failure, refresh token collision`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userRepository.users = mutableListOf(testUserEntity)
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_in") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Token collision detected.")
    }

    @Test
    fun `Sign in, failure, saveRefreshToken failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userRepository.users = mutableListOf(testUserEntity)
        refreshTokenRepository.shouldSave = false
        val body = Json.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_in") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Couldn't save refresh token.")
    }

    @Test
    fun `Sign out, success`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/sign_out") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `Sign out, failure, invalid refresh token`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.shouldDelete = false
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/sign_out") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Invalid refresh token.")
    }

    @Test
    fun `Refresh tokens, success`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val tokenResponse = Json.decodeFromString<TokenResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(tokenResponse.refreshToken).isEqualTo(testRefreshToken)
        assertThat(tokenResponse.accessToken).isEqualTo(testAccessToken)
    }

    @Test
    fun `Refresh tokens, failure, isValid is false`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        tokenService.shouldSucceed = false
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Invalid refresh token.")
    }

    @Test
    fun `Refresh tokens, failure, token entity not found`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf()
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Token not found.")
    }

    @Test
    fun `Refresh tokens, failure, token was revoked`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity.copy(isRevoked = true))
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Token has been revoked or expired.")
    }

    @Test
    fun `Refresh tokens, failure, token expired`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens =
            mutableListOf(testRefreshTokenEntity.copy(expiresAt = System.currentTimeMillis() - 60000L))
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Token has been revoked or expired.")
    }

    @Test
    fun `Refresh tokens, failure, revokeToken failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        refreshTokenRepository.shouldDelete = false
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Unable to remove refresh token.")
    }

    @Test
    fun `Refresh tokens, failure, token claims don't contain userID`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        tokenService.claims = mapOf()
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Invalid token payload.")
    }

    @Test
    fun `Refresh tokens, failure, refresh token collision`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        refreshTokenRepository.shouldGet2ndTime = false
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.name)
        assertThat(errorResponse.message).isEqualTo("Token collision detected.")
    }

    @Test
    fun `Refresh tokens, failure, saveRefreshToken failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        refreshTokenRepository.shouldSave = false
        refreshTokenRepository.tokens = mutableListOf(testRefreshTokenEntity)
        val body = Json.encodeToString(RefreshTokenRequest(testRefreshToken))

        val response = client.post("/refresh") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Couldn't save refresh token.")
    }
}