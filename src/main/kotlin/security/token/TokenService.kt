package security.token

interface TokenService {
    fun generateAccessToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun generateRefreshToken(config: TokenConfig, vararg claims: TokenClaim): String
    fun verifyToken(token: String, config: TokenConfig): TokenValidationResult
}

data class TokenValidationResult(
    val isValid: Boolean,
    val claims: Map<String, String> = emptyMap()
)