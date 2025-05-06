package security.token

import model.auth.testAccessToken
import model.auth.testRefreshToken
import model.security.TokenClaim
import model.security.TokenConfig
import model.security.TokenValidationResult
import model.user.testUserID

class FakeTokenService : TokenService {
    var accessToken = testAccessToken
    var refreshToken = testRefreshToken
    var shouldSucceed: Boolean = true
    var claims = mapOf("userID" to testUserID)

    override fun generateAccessToken(
        config: TokenConfig,
        vararg claims: TokenClaim
    ): String {
        return accessToken
    }

    override fun generateRefreshToken(
        config: TokenConfig,
        vararg claims: TokenClaim
    ): String {
        return refreshToken
    }

    override fun verifyToken(
        token: String,
        config: TokenConfig
    ): TokenValidationResult {
        return TokenValidationResult(
            isValid = shouldSucceed,
            claims = claims
        )
    }
}