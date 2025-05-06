package repository

import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.test.runTest
import mapper.toUser
import model.user.UserEntity
import model.user.testUserEntity
import model.user.testUserID
import org.bson.conversions.Bson
import util.BaseTestWithoutKoin
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryImplTest : BaseTestWithoutKoin() {
    private lateinit var userRepository: UserRepositoryImpl
    val userEntities: MongoCollection<UserEntity> = mockk(relaxed = true)

    @BeforeTest
    fun setUp() {
        userRepository = UserRepositoryImpl(userEntities)
    }

    @Test
    fun `getUsers - user found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<UserEntity>>()
            collector.emit(testUserEntity)
            true
        }

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.getUsers()

        assertEquals(listOf(testUserEntity.toUser()), result)
        coVerify { userEntities.find(any<Bson>()) }
    }

    @Test
    fun `getUsers - no users found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } returns Unit

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.getUsers()

        assertEquals(emptyList(), result)
        coVerify { userEntities.find(any<Bson>()) }
    }

    @Test
    fun `getUserByID - user found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<UserEntity>>()
            collector.emit(testUserEntity)
            true
        }

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.getUserByID(testUserID)

        assertEquals(testUserEntity.toUser(), result)
        coVerify { userEntities.find(any<Bson>()) }
    }

    @Test
    fun `getUserByID - user not found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } returns Unit

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.getUserByID(testUserID)

        assertNull(result)
        coVerify { userEntities.find(any<Bson>()) }
    }

    @Test
    fun `getUserEntityByEmail - user found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<UserEntity>>()
            collector.emit(testUserEntity)
            true
        }

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.getUserEntityByEmail(testUserID)

        assertEquals(testUserEntity, result)
        coVerify { userEntities.find(any<Bson>()) }
    }

    @Test
    fun `getUserEntityByEmail - user not found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } returns Unit

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.getUserEntityByEmail(testUserID)

        assertNull(result)
        coVerify { userEntities.find(any<Bson>()) }
    }


    @Test
    fun `removeUser - success`() = runTest {
        coEvery {
            userEntities.deleteOne(
                filter = any<Bson>(),
                options = any<DeleteOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns true
        }

        val result = userRepository.removeUser(testUserID)
        assertTrue(result)
        coVerify {
            userEntities.deleteOne(
                filter = any<Bson>(),
                options = any<DeleteOptions>()
            )
        }
    }


    @Test
    fun `removeUser - failure`() = runTest {
        coEvery {
            userEntities.deleteOne(
                filter = any<Bson>(),
                options = any<DeleteOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns false
        }

        val result = userRepository.removeUser(testUserID)
        assertFalse(result)
        coVerify {
            userEntities.deleteOne(
                filter = any<Bson>(),
                options = any<DeleteOptions>()
            )
        }
    }


    @Test
    fun `addUserEntity - success`() = runTest {
        coEvery {
            userEntities.insertOne(
                document = any<UserEntity>(),
                options = any<InsertOneOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns true
        }

        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<UserEntity>>()
            collector.emit(testUserEntity)
            true
        }

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.addUserEntity(testUserEntity)

        assertEquals(testUserID, result)
        coVerify {
            userEntities.insertOne(
                document = testUserEntity,
                options = any<InsertOneOptions>()
            )
        }
        coVerify {
            userEntities.find(any<Bson>())
        }
    }


    @Test
    fun `addUserEntity - insert not acknowledged`() = runTest {
        coEvery {
            userEntities.insertOne(
                document = any<UserEntity>(),
                options = any<InsertOneOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns false
        }

        val result = userRepository.addUserEntity(testUserEntity)

        assertNull(result)
        coVerify {
            userEntities.insertOne(
                document = testUserEntity,
                options = any<InsertOneOptions>()
            )
        }
    }


    @Test
    fun `addUserEntity - user not found`() = runTest {
        coEvery {
            userEntities.insertOne(
                document = any<UserEntity>(),
                options = any<InsertOneOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns true
        }

        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } returns Unit

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.addUserEntity(testUserEntity)

        assertNull(result)
        coVerify {
            userEntities.insertOne(
                document = testUserEntity,
                options = any<InsertOneOptions>()
            )
        }
        coVerify {
            userEntities.find(any<Bson>())
        }
    }


    @Test
    fun `updateUser - success`() = runTest {
        coEvery {
            userEntities.replaceOne(
                filter = any<Bson>(),
                replacement = any<UserEntity>(),
                options = any<ReplaceOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns true
        }

        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<UserEntity>>()
            collector.emit(testUserEntity)
            true
        }

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.updateUser(testUserEntity.toUser())

        assertTrue(result)
        coVerify {
            userEntities.replaceOne(
                filter = any<Bson>(),
                replacement = any<UserEntity>(),
                options = any<ReplaceOptions>()
            )
        }
        coVerify {
            userEntities.find(any<Bson>())
        }
    }


    @Test
    fun `updateUser - user not found`() = runTest {
        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } returns Unit

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.updateUser(testUserEntity.toUser())

        assertFalse(result)
        coVerify {
            userEntities.find(any<Bson>())
        }
    }


    @Test
    fun `updateUser - failure`() = runTest {
        coEvery {
            userEntities.replaceOne(
                filter = any<Bson>(),
                replacement = any<UserEntity>(),
                options = any<ReplaceOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns false
        }

        val mockFindFlow = mockk<FindFlow<UserEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<UserEntity>>()
            collector.emit(testUserEntity)
            true
        }

        coEvery {
            userEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = userRepository.updateUser(testUserEntity.toUser())

        assertFalse(result)
        coVerify {
            userEntities.replaceOne(
                filter = any<Bson>(),
                replacement = any<UserEntity>(),
                options = any<ReplaceOptions>()
            )
        }
        coVerify {
            userEntities.find(any<Bson>())
        }
    }
}