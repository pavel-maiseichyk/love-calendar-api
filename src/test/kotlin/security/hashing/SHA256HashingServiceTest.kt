package security.hashing

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import model.user.testPassword
import org.apache.commons.codec.digest.DigestUtils
import kotlin.test.BeforeTest
import kotlin.test.Test

class SHA256HashingServiceTest {
    private lateinit var hashingService: SHA256HashingService

    @BeforeTest
    fun setUp() {
        hashingService = SHA256HashingService()
    }

    @Test
    fun `generateSaltedHash should return correct salted hash`() {
        val saltLength = 16

        val saltedHash = hashingService.generateSaltedHash(testPassword, saltLength)

        assertThat(saltedHash.hash).isNotNull()
        assertThat(saltedHash.salt).isNotNull()
        assertThat(saltLength * 2).isEqualTo(saltedHash.salt.length)

        val expectedHash = DigestUtils.sha256Hex(saltedHash.salt + testPassword)
        assertThat(expectedHash).isEqualTo(saltedHash.hash)
    }

    @Test
    fun `verify should return true if hash is correct`() {
        val saltLength = 16

        val saltedHash = hashingService.generateSaltedHash(testPassword, saltLength)

        assertThat(hashingService.verify(testPassword, saltedHash)).isTrue()
    }

    @Test
    fun `verify should return false if hash is incorrect`() {
        val wrongPassword = "password"
        val saltLength = 16

        val saltedHash = hashingService.generateSaltedHash(testPassword, saltLength)

        assertThat(hashingService.verify(wrongPassword, saltedHash)).isFalse()
    }
}