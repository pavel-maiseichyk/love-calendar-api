package route

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.mongodb.WriteConcern
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import model.core.ApiException
import model.core.ErrorResponse
import model.auth.RefreshTokenEntity
import model.user.UserEntity
import model.user.testEmail
import model.user.testPassword
import model.user.testUserEntity
import repository.RefreshTokenRepositoryImpl
import repository.UserRepositoryImpl
import model.auth.AuthRequest
import model.auth.TokenResponse
import model.security.testConfig
import security.hashing.SHA256HashingService
import security.token.JWTTokenService
import util.BaseTestWithoutKoin
import util.Constants.MONGO_CONNECTION_STRING
import util.Constants.REFRESH_TOKENS_COLLECTION_NAME
import util.Constants.TEST_DATABASE_NAME
import util.Constants.USERS_COLLECTION_NAME
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AuthRoutesInstrumentedTest : BaseTestWithoutKoin() {

    lateinit var db: MongoDatabase
    lateinit var userEntities: MongoCollection<UserEntity>
    lateinit var tokenEntities: MongoCollection<RefreshTokenEntity>
    lateinit var hashingService: SHA256HashingService
    lateinit var tokenService: JWTTokenService
    lateinit var userRepository: UserRepositoryImpl
    lateinit var refreshTokenRepository: RefreshTokenRepositoryImpl

    @BeforeTest
    fun setUp() = runTest {
        db = MongoClient.create(MONGO_CONNECTION_STRING).getDatabase(databaseName = TEST_DATABASE_NAME)
        userEntities = db.getCollection<UserEntity>(USERS_COLLECTION_NAME)
        userRepository = UserRepositoryImpl(users = userEntities)
        tokenEntities = db.getCollection<RefreshTokenEntity>(REFRESH_TOKENS_COLLECTION_NAME)
        refreshTokenRepository = RefreshTokenRepositoryImpl(refreshTokens = tokenEntities)
        hashingService = SHA256HashingService()
        tokenService = JWTTokenService()
    }

    @AfterTest
    fun tearDown() = runTest {
        db.drop()
    }

    @Test
    fun `Sign up, success`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        val body = Json.Default.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val tokenResponseResult = Json.Default.decodeFromString<TokenResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.OK)
        assertThat(tokenService.verifyToken(tokenResponseResult.refreshToken, config = testConfig).isValid).isTrue()
        assertThat(tokenService.verifyToken(tokenResponseResult.accessToken, config = testConfig).isValid).isTrue()
    }

    @Test
    fun `Sign up, failure, email exists`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = refreshTokenRepository
    ) {
        userEntities.insertOne(testUserEntity)
        val body = Json.Default.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.Conflict)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.USER_ALREADY_EXISTS.name)
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
        val body = Json.Default.encodeToString(AuthRequest(email = "blablabla", password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.BadRequest)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.INVALID_EMAIL_FORMAT.name)
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
        val body = Json.Default.encodeToString(AuthRequest(email = testEmail, password = "123"))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.BadRequest)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.INVALID_PASSWORD_LENGTH.name)
        assertThat(errorResponse.message).isEqualTo("Password length is invalid.")
    }

    @Test
    fun `Sign up, failure, addUserEntity failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = UserRepositoryImpl(users = userEntities.withWriteConcern(WriteConcern.UNACKNOWLEDGED)),
        refreshTokenRepository = refreshTokenRepository
    ) {
        val body = Json.Default.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Failed to add user.")
    }


    @Test
    fun `Sign up, failure, saveRefreshToken failed`() = testWithAuthRoutes(
        hashingService = hashingService,
        tokenService = tokenService,
        tokenConfig = testConfig,
        userRepository = userRepository,
        refreshTokenRepository = RefreshTokenRepositoryImpl(refreshTokens = tokenEntities.withWriteConcern(WriteConcern.UNACKNOWLEDGED))
    ) {
        val body = Json.Default.encodeToString(AuthRequest(email = testEmail, password = testPassword))

        val response = client.post("/sign_up") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Couldn't save refresh token.")
    }
}