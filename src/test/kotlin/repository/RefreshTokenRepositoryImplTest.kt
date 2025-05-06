package repository

import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import model.auth.RefreshTokenEntity
import model.auth.testRefreshToken
import model.auth.testRefreshTokenEntity
import org.bson.conversions.Bson
import util.BaseTestWithoutKoin
import kotlin.test.*

class RefreshTokenRepositoryImplTest : BaseTestWithoutKoin() {
    private lateinit var tokenRepository: RefreshTokenRepositoryImpl
    val tokenEntities: MongoCollection<RefreshTokenEntity> = mockk(relaxed = true)

    @BeforeTest
    fun setUp() {
        tokenRepository = RefreshTokenRepositoryImpl(tokenEntities)
    }

    @Test
    fun `saveRefreshToken - success`() = runBlocking {
        coEvery {
            tokenEntities.insertOne(
                document = any<RefreshTokenEntity>(),
                options = any<InsertOneOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns true
        }

        val result = tokenRepository.saveRefreshToken(testRefreshTokenEntity)
        assertTrue(result)
        coVerify {
            tokenEntities.insertOne(
                document = any<RefreshTokenEntity>(),
                options = any<InsertOneOptions>()
            )
        }
    }

    @Test
    fun `saveRefreshToken - failure`() = runBlocking {
        coEvery {
            tokenEntities.insertOne(
                document = any<RefreshTokenEntity>(),
                options = any<InsertOneOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns false
        }

        val result = tokenRepository.saveRefreshToken(testRefreshTokenEntity)
        assertFalse(result)
        coVerify {
            tokenEntities.insertOne(
                document = any<RefreshTokenEntity>(),
                options = any<InsertOneOptions>()
            )
        }
    }

    @Test
    fun `getEntityByToken - entity found`() = runBlocking {
        val mockFindFlow = mockk<FindFlow<RefreshTokenEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } coAnswers {
            val collector = firstArg<FlowCollector<RefreshTokenEntity>>()
            collector.emit(testRefreshTokenEntity)
            true
        }

        coEvery {
            tokenEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = tokenRepository.getEntityByToken(testRefreshToken)

        assertEquals(testRefreshTokenEntity, result)
        coVerify { tokenEntities.find(any<Bson>()) }
    }

    @Test
    fun `getEntityByToken - entity not found`() = runBlocking {
        val mockFindFlow = mockk<FindFlow<RefreshTokenEntity>>()
        coEvery {
            mockFindFlow.collect(any())
        } returns Unit

        coEvery {
            tokenEntities.find(any<Bson>())
        } returns mockFindFlow

        val result = tokenRepository.getEntityByToken(testRefreshToken)

        assertNull(result)
        coVerify { tokenEntities.find(any<Bson>()) }
    }

    @Test
    fun `revokeToken - success`() = runBlocking {
        coEvery {
            tokenEntities.updateOne(
                filter = any<Bson>(),
                update = any<Bson>(),
                options = any<UpdateOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns true
        }

        val result = tokenRepository.revokeToken(testRefreshToken)
        assertTrue(result)
        coVerify {
            tokenEntities.updateOne(
                filter = any<Bson>(),
                update = any<Bson>(),
                options = any<UpdateOptions>()
            )
        }
    }

    @Test
    fun `revokeToken - failure`() = runBlocking {
        coEvery {
            tokenEntities.updateOne(
                filter = any<Bson>(),
                update = any<Bson>(),
                options = any<UpdateOptions>()
            )
        } returns mockk {
            every { wasAcknowledged() } returns false
        }

        val result = tokenRepository.revokeToken(testRefreshToken)
        assertFalse(result)
        coVerify {
            tokenEntities.updateOne(
                filter = any<Bson>(),
                update = any<Bson>(),
                options = any<UpdateOptions>()
            )
        }
    }
}