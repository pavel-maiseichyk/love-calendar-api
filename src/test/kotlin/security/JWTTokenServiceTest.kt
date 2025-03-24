package com.example.security

import com.auth0.jwt.JWT
import security.token.JWTTokenService
import security.token.TokenClaim
import security.token.TokenConfig
import java.util.*
import kotlin.test.*

class JWTTokenServiceTest {

    private val jwtTokenService = JWTTokenService()

    @Test
    fun `generate should return a valid token with correct claims`() {
        val config = TokenConfig(
            issuer = "issuer",
            audience = "audience",
            expiresIn = 3600L,
            secret = "secret"
        )
        val claim1 = TokenClaim(name = "claim1", value = "value1")
        val claim2 = TokenClaim(name = "claim2", value = "value2")

        val token = jwtTokenService.generate(config, claim1, claim2)

        val decodedJWT = JWT.decode(token)
        assertEquals(config.audience, decodedJWT.audience[0])
        assertEquals(config.issuer, decodedJWT.issuer)
        assertTrue(decodedJWT.expiresAt.after(Date(System.currentTimeMillis())))
        assertEquals("value1", decodedJWT.getClaim("claim1").asString())
        assertEquals("value2", decodedJWT.getClaim("claim2").asString())
    }

    @Test
    fun `generate should create token with valid expiration time`() {
        val config = TokenConfig(
            issuer = "issuer",
            audience = "audience",
            expiresIn = 1000L,
            secret = "secret"
        )
        val claim = TokenClaim(name = "claim1", value = "value1")

        val token = jwtTokenService.generate(config, claim)
        val decodedJWT = JWT.decode(token)
        val expiresAt = decodedJWT.expiresAt
        assertNotNull(expiresAt)
        assertTrue(expiresAt.after(Date(System.currentTimeMillis())))
    }

    @Test
    fun `generate should correctly encode the claims in the token`() {
        val config = TokenConfig(
            issuer = "issuer",
            audience = "audience",
            expiresIn = 3600L,
            secret = "secret"
        )
        val claim1 = TokenClaim(name = "claim1", value = "value1")
        val claim2 = TokenClaim(name = "claim2", value = "value2")

        val token = jwtTokenService.generate(config, claim1, claim2)

        val decodedJWT = JWT.decode(token)
        assertEquals("value1", decodedJWT.getClaim("claim1").asString())
        assertEquals("value2", decodedJWT.getClaim("claim2").asString())
    }

    @Test
    fun `generate should sign with correct algorithm`() {
        val config = TokenConfig(
            issuer = "issuer",
            audience = "audience",
            expiresIn = 3600L,
            secret = "secret"
        )
        val claim1 = TokenClaim(name = "claim1", value = "value1")

        val token = jwtTokenService.generate(config, claim1)

        val decodedJWT = JWT.decode(token)
        assertNotNull(decodedJWT)
        assertEquals("issuer", decodedJWT.issuer)
        assertEquals("audience", decodedJWT.audience[0])
    }
}