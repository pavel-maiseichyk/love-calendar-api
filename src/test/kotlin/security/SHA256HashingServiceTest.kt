package com.example.security

import org.apache.commons.codec.digest.DigestUtils
import org.junit.Assert.*
import org.junit.Test
import io.mockk.*
import security.hashing.SHA256HashingService
import java.security.SecureRandom

class SHA256HashingServiceTest {

    private val hashingService = SHA256HashingService()

    @Test
    fun `generateSaltedHash should return correct salted hash`() {
        val value = "mySecretPassword"
        val saltLength = 16

        val saltedHash = hashingService.generateSaltedHash(value, saltLength)

        assertNotNull(saltedHash.hash)
        assertNotNull(saltedHash.salt)

        assertEquals(saltLength * 2, saltedHash.salt.length)

        val expectedHash = DigestUtils.sha256Hex(saltedHash.salt + value)
        assertEquals(expectedHash, saltedHash.hash)
    }

    @Test
    fun `verify should return true if hash is correct`() {
        val value = "mySecretPassword"
        val saltLength = 16

        val saltedHash = hashingService.generateSaltedHash(value, saltLength)

        assertTrue(hashingService.verify(value, saltedHash))
    }

    @Test
    fun `verify should return false if hash is incorrect`() {
        val value = "mySecretPassword"
        val wrongValue = "wrongPassword"
        val saltLength = 16

        val saltedHash = hashingService.generateSaltedHash(value, saltLength)

        assertFalse(hashingService.verify(wrongValue, saltedHash))
    }

    @Test
    fun `generateSaltedHash should use SecureRandom`() {
        mockkStatic(SecureRandom::class)
        val mockSecureRandom = mockk<SecureRandom>(relaxed = true)
        every { SecureRandom.getInstance("SHA1PRNG") } returns mockSecureRandom

        hashingService.generateSaltedHash("value", 16)

        verify { mockSecureRandom.generateSeed(16) }
        unmockkStatic(SecureRandom::class)
    }
}