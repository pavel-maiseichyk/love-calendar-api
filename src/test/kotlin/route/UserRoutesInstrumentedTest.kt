package route

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.mongodb.WriteConcern
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mapper.toUser
import model.core.ApiException
import model.core.ErrorResponse
import model.user.UserEntity
import model.user.UserResponse
import model.user.testUserEntity
import model.user.testUserID
import repository.UserRepositoryImpl
import util.BaseTestWithoutKoin
import util.Constants.MONGO_CONNECTION_STRING
import util.Constants.TEST_DATABASE_NAME
import util.Constants.USERS_COLLECTION_NAME
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserRoutesInstrumentedTest : BaseTestWithoutKoin() {
    lateinit var userRepository: UserRepositoryImpl
    lateinit var db: MongoDatabase
    lateinit var userEntities: MongoCollection<UserEntity>

    @BeforeTest
    fun setUp() {
        db = MongoClient.create(MONGO_CONNECTION_STRING).getDatabase(databaseName = TEST_DATABASE_NAME)
        userEntities = db.getCollection<UserEntity>(USERS_COLLECTION_NAME)
        userRepository = UserRepositoryImpl(users = userEntities)
    }

    @AfterTest
    fun tearDown() = runTest {
        db.drop()
    }

    @Test
    fun `Get user, success`() = testWithUserRoutes(userRepository) {
        userEntities.insertOne(testUserEntity)

        val response = client.get("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.OK)

        val userResponse = Json.Default.decodeFromString<UserResponse>(response.bodyAsText())
        assertThat(testUserEntity.toUser()).isDataClassEqualTo(userResponse.user)
    }

    @Test
    fun `Get user, user not found`() = testWithUserRoutes(userRepository) {
        val response = client.get("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.NotFound)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.USER_NOT_FOUND.name)
        assertThat(errorResponse.message).isEqualTo("User not found.")
    }

    @Test
    fun `Update user, success`() = testWithUserRoutes(userRepository) {
        userEntities.insertOne(testUserEntity)
        val body = Json.Default.encodeToString(testUserEntity.toUser())

        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(body)
            bearerAuth(token = generateTestJWT(testUserID))
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.OK)
    }

    @Test
    fun `Update user, fails due to repository`() = testWithUserRoutes(
        repository = UserRepositoryImpl(userEntities.withWriteConcern(WriteConcern.UNACKNOWLEDGED))
    ) {
        val body = Json.Default.encodeToString(testUserEntity.toUser())

        val response = client.put("/users") {
            contentType(ContentType.Application.Json)
            setBody(body)
            bearerAuth(token = generateTestJWT(testUserID))
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.InternalServerError)
        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Unable to update user.")
    }

    @Test
    fun `Delete user, success`() = testWithUserRoutes(userRepository) {
        userEntities.insertOne(testUserEntity)

        val response = client.delete("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.OK)
    }

    @Test
    fun `Delete user, fails due to repository`() = testWithUserRoutes(
        repository = UserRepositoryImpl(userEntities.withWriteConcern(WriteConcern.UNACKNOWLEDGED))
    ) {
        val response = client.delete("/users") {
            bearerAuth(token = generateTestJWT(testUserID))
        }

        val errorResponse = Json.Default.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertThat(response.status).isEqualTo(HttpStatusCode.Companion.InternalServerError)
        assertThat(errorResponse.errorCode).isEqualTo(ApiException.ErrorCode.INTERNAL_SERVER_ERROR.name)
        assertThat(errorResponse.message).isEqualTo("Unable to delete user.")
    }
}