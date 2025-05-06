package model.security

val testConfig = TokenConfig(
    issuer = "test-issuer",
    audience = "test-audience",
    secret = "test-secret-key-that-is-long-enough-for-testing",
    accessTokenExpiration = 3600000, // 1 hour
    refreshTokenExpiration = 86400000 // 24 hours
)