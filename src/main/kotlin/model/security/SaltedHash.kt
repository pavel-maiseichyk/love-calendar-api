package model.security

data class SaltedHash(
    val hash: String,
    val salt: String
)
