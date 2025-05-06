package security.token

import model.security.TokenClaim
import model.security.TokenConfig
import model.security.TokenValidationResult

interface TokenService {
    fun generateAccessToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun generateRefreshToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun verifyToken(token: String, config: TokenConfig): TokenValidationResult
}