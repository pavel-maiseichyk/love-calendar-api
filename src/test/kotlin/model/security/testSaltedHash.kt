package model.security

const val testSalt = "test-salt"
const val testHash = "test-hash"

val testSaltedHash = SaltedHash(
    hash = testHash,
    salt = testSalt
)