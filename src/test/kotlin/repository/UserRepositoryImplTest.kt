package com.example.repository

import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import io.mockk.*
import kotlinx.coroutines.runBlocking
import mappers.toUser
import models.UserEntity
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import repository.UserRepositoryImpl
import kotlin.test.*

class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl
    private val userCollection = mockk<CoroutineCollection<UserEntity>>(relaxed = true)

    private val userID = "123"
    private val userEntity = UserEntity(
        id = userID,
        email = "test@example.com",
        password = "hashed",
        salt = "salt",
        name = "Paul",
        specialDate = "",
        meetings = emptyList()
    )

    @BeforeTest
    fun setUp() {
        repository = UserRepositoryImpl(userCollection)
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getUsers should return list of users`() = runBlocking {
        val userEntities = listOf(userEntity)

        coEvery { userCollection.find().toList() } returns userEntities
        val result = repository.getUsers()

        assertEquals(userEntities.map { it.toUser() }, result)
        coVerify { userCollection.find().toList() }
    }

    @Test
    fun `getUserByID should return user`() = runBlocking {
        val testID = "1"
        coEvery { userCollection.findOne(UserEntity::id eq testID) } returns userEntity

        val result = repository.getUserByID(userID = testID)
        assertEquals(userEntity.toUser(), result)
        coVerify { userCollection.findOne(UserEntity::id eq testID) }
    }

    @Test
    fun `getUserByID should return null if user doesn't exist`() = runBlocking {
        val testID = "1"
        coEvery { userCollection.findOne(UserEntity::id eq testID) } returns null

        val result = repository.getUserByID(userID = testID)
        assertNull(result)
        coVerify { userCollection.findOne(UserEntity::id eq testID) }
    }

    @Test
    fun `getUserEntityByEmail should return userEntity`() = runBlocking {
        val testEmail = "test@test.com"
        coEvery { userCollection.findOne(UserEntity::email eq testEmail) } returns userEntity

        val result = repository.getUserEntityByEmail(email = testEmail)
        assertEquals(userEntity, result)
        coVerify { userCollection.findOne(UserEntity::email eq testEmail) }
    }

    @Test
    fun `getUserEntityByEmail should return null if user doesn't exist`() = runBlocking {
        val testEmail = "test@test.com"
        coEvery { userCollection.findOne(UserEntity::email eq testEmail) } returns null

        val result = repository.getUserEntityByEmail(email = testEmail)
        assertNull(result)
        coVerify { userCollection.findOne(UserEntity::email eq testEmail) }
    }

    @Test
    fun `addUserEntity should return true if insert was acknowledged`() = runBlocking {
        coEvery { userCollection.insertOne(any<UserEntity>(), any<InsertOneOptions>()).wasAcknowledged() } returns true

        val result = repository.addUserEntity(userEntity)
        assertTrue(result)
        coVerify { userCollection.insertOne(any<UserEntity>(), any<InsertOneOptions>()).wasAcknowledged() }
    }

    @Test
    fun `addUserEntity should return false if insert wasn't acknowledged`() = runBlocking {
        coEvery { userCollection.insertOne(any<UserEntity>(), any<InsertOneOptions>()).wasAcknowledged() } returns false

        val result = repository.addUserEntity(userEntity = userEntity)
        assertFalse(result)
        coVerify { userCollection.insertOne(any<UserEntity>(), any<InsertOneOptions>()).wasAcknowledged() }
    }

    @Test
    fun `updateUser should return true if replace was acknowledged`() = runBlocking {
        val replaceResultMock = mockk<UpdateResult> {
            every { wasAcknowledged() } returns true
        }
        coEvery { userCollection.findOne(UserEntity::id eq userID) } returns userEntity
        coEvery {
            userCollection.replaceOne(
                eq(UserEntity::id eq userID),
                any<UserEntity>(),
                any<ReplaceOptions>()
            )
        } returns replaceResultMock

        val result = repository.updateUser(updatedUser = userEntity.toUser())
        assertTrue(result)
        coVerify {
            userCollection.replaceOne(
                eq(UserEntity::id eq userID),
                any<UserEntity>(),
                any<ReplaceOptions>()
            )
        }
    }

    @Test
    fun `updateUser should return false if replace wasn't acknowledged`() = runBlocking {
        val replaceResultMock = mockk<UpdateResult> {
            every { wasAcknowledged() } returns false
        }
        coEvery { userCollection.findOne(UserEntity::id eq userID) } returns userEntity
        coEvery {
            userCollection.replaceOne(
                eq(UserEntity::id eq userID),
                any<UserEntity>(),
                any<ReplaceOptions>()
            )
        } returns replaceResultMock

        val result = repository.updateUser(updatedUser = userEntity.toUser())
        assertFalse(result)
        coVerify {
            userCollection.replaceOne(
                eq(UserEntity::id eq userID),
                any<UserEntity>(),
                any<ReplaceOptions>()
            )
        }
    }

    @Test
    fun `updateUser should return false if user wasn't found`() = runBlocking {
        coEvery { userCollection.findOne(UserEntity::id eq userID) } returns null

        val result = repository.updateUser(updatedUser = userEntity.toUser())
        assertFalse(result)
        coVerify {
            userCollection.findOne(UserEntity::id eq userID)
        }
    }

    @Test
    fun `removeUser should return true if delete was acknowledged`() = runBlocking {
        val deleteResultMock = mockk<DeleteResult>(relaxed = true) {
            every { wasAcknowledged() } returns true
        }

        coEvery {
            userCollection.deleteOne(
                eq(UserEntity::id eq userID),
                any<DeleteOptions>()
            )
        } returns deleteResultMock

        val result = repository.removeUser(userID = userID)
        assertTrue(result)
        coVerify {
            userCollection.deleteOne(
                eq(UserEntity::id eq userID),
                any<DeleteOptions>()
            )
        }
    }

    @Test
    fun `removeUser should return false if delete wasn't acknowledged`() = runBlocking {
        val deleteResultMock = mockk<DeleteResult>(relaxed = true) {
            every { wasAcknowledged() } returns false
        }

        coEvery {
            userCollection.deleteOne(
                eq(UserEntity::id eq userID),
                any<DeleteOptions>()
            )
        } returns deleteResultMock

        val result = repository.removeUser(userID = userID)
        assertFalse(result)
        coVerify {
            userCollection.deleteOne(
                eq(UserEntity::id eq userID),
                any<DeleteOptions>()
            )
        }
    }
}