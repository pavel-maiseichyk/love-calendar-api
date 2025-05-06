package security.token

import assertk.assertThat
import assertk.assertions.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import model.security.TokenClaim
import model.security.testConfig
import model.user.testUserID
import kotlin.test.Test

class JWTTokenServiceTest {
    private val tokenService = JWTTokenService()

    @Test
    fun `generateAccessToken creates token with correct claims`() {
        val roleClaim = TokenClaim("role", "admin")

        val token = tokenService.generateAccessToken(
            config = testConfig,
            claims = arrayOf(TokenClaim("userID", testUserID), roleClaim)
        )
        val decoded = JWT.require(Algorithm.HMAC256(testConfig.secret))
            .withIssuer(testConfig.issuer)
            .withAudience(testConfig.audience)
            .build()
            .verify(token)

        assertThat(testConfig.issuer).isEqualTo(decoded.issuer)
        assertThat(testConfig.audience).isEqualTo(decoded.audience[0])
        assertThat(testUserID).isEqualTo(decoded.getClaim("userID").asString())
        assertThat("admin").isEqualTo(decoded.getClaim("role").asString())
        assertThat("access").isEqualTo(decoded.getClaim("type").asString())
        assertThat(decoded.id).isNotNull()

        val expectedExpirationTimeMs = System.currentTimeMillis() + testConfig.accessTokenExpiration
        assertThat(decoded.expiresAt.time <= expectedExpirationTimeMs).isTrue()
    }


    @Test
    fun `generateRefreshToken creates token with correct claims`() {
        val token = tokenService.generateRefreshToken(
            config = testConfig,
            claims = arrayOf(TokenClaim("userID", testUserID))
        )

        val decoded = JWT.require(Algorithm.HMAC256(testConfig.secret))
            .withIssuer(testConfig.issuer)
            .withAudience(testConfig.audience)
            .build()
            .verify(token)

        assertThat(testConfig.issuer).isEqualTo(decoded.issuer)
        assertThat(testConfig.audience).isEqualTo(decoded.audience[0])
        assertThat(testUserID).isEqualTo(decoded.getClaim("userID").asString())
        assertThat("refresh").isEqualTo(decoded.getClaim("type").asString())
        assertThat(decoded.id).isNotNull()

        val expectedExpirationTimeMs = System.currentTimeMillis() + testConfig.refreshTokenExpiration
        assertThat(decoded.expiresAt.time <= expectedExpirationTimeMs).isTrue()
    }

    @Test
    fun `verifyToken returns valid result for valid token`() {
        val token = tokenService.generateAccessToken(
            config = testConfig,
            claims = arrayOf(TokenClaim("userID", testUserID))
        )

        val result = tokenService.verifyToken(token, testConfig)

        assertThat(result.isValid).isTrue()
        assertThat(result.claims).isNotNull()
        assertThat(testUserID).isEqualTo(result.claims["userID"])
        assertThat("access").isEqualTo(result.claims["type"])
    }

    @Test
    fun `verifyToken returns invalid result for expired token`() {
        val expiredConfig = testConfig.copy(accessTokenExpiration = -10000) // Expired 10 seconds ago
        val token = tokenService.generateAccessToken(
            config = expiredConfig,
            claims = arrayOf(TokenClaim("userID", testUserID))
        )

        val result = tokenService.verifyToken(token, testConfig)

        assertThat(result.isValid).isFalse()
        assertThat(result.claims).isEmpty()
    }

    @Test
    fun `verifyToken returns invalid result for token with wrong issuer`() {
        val token = tokenService.generateAccessToken(
            config = testConfig,
            claims = arrayOf(TokenClaim("userID", testUserID))
        )

        val differentConfig = testConfig.copy(issuer = "different-issuer")
        val result = tokenService.verifyToken(token, differentConfig)

        assertThat(result.isValid).isFalse()
        assertThat(result.claims).isEmpty()
    }

    @Test
    fun `verifyToken returns invalid result for token with wrong audience`() {
        val token = tokenService.generateAccessToken(
            config = testConfig,
            claims = arrayOf(TokenClaim("userID", testUserID))
        )

        val differentConfig = testConfig.copy(audience = "different-audience")
        val result = tokenService.verifyToken(token, differentConfig)

        assertThat(result.isValid).isFalse()
        assertThat(result.claims).isEmpty()
    }

    @Test
    fun `verifyToken returns invalid result for token with wrong secret`() {
        val token = tokenService.generateAccessToken(
            config = testConfig,
            claims = arrayOf(TokenClaim("userID", testUserID))
        )

        val differentConfig = testConfig.copy(secret = "different-secret-key-also-long-enough")
        val result = tokenService.verifyToken(token, differentConfig)

        assertThat(result.isValid).isFalse()
        assertThat(result.claims).isEmpty()
    }

    @Test
    fun `verifyToken returns invalid result for malformed token`() {
        val malformedToken = "not.a.jwt.token"

        val result = tokenService.verifyToken(malformedToken, testConfig)

        assertThat(result.isValid).isFalse()
        assertThat(result.claims).isEmpty()
    }
}