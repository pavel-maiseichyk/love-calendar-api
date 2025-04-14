package security.token

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val secret: String,
    val accessTokenExpiration: Long = 1000 * 60 * 15, // 15 minutes
    val refreshTokenExpiration: Long = 1000 * 60 * 60 * 24 * 7 // 7 days
)
