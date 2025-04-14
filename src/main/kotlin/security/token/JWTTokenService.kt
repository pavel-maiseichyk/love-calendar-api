package security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.*

class JWTTokenService : TokenService {
    override fun generateAccessToken(config: TokenConfig, vararg claims: TokenClaim): String {
        return generateToken(config, config.accessTokenExpiration, "access", *claims)
    }

    override fun generateRefreshToken(config: TokenConfig, vararg claims: TokenClaim): String {
        return generateToken(config, config.refreshTokenExpiration, "refresh", *claims)
    }

    private fun generateToken(
        config: TokenConfig,
        expirationMs: Long,
        tokenType: String,
        vararg claims: TokenClaim
    ): String {
        val jti = UUID.randomUUID().toString()
        var token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
            .withJWTId(jti)
            .withClaim("type", tokenType)

        claims.forEach { claim ->
            token = token.withClaim(claim.name, claim.value)
        }
        return token.sign(Algorithm.HMAC256(config.secret))
    }

    override fun verifyToken(token: String, config: TokenConfig): TokenValidationResult {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(config.secret))
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .build()

            val decodedJWT = verifier.verify(token)
            val claims = decodedJWT.claims.mapValues { it.value.asString() }

            TokenValidationResult(isValid = true, claims = claims)
        } catch (e: JWTVerificationException) {
            TokenValidationResult(isValid = false)
        }
    }
}