package model.user

import model.security.testHash
import model.security.testSalt

const val testUserID = "test-user-id"
const val testEmail = "test@example.com"
const val testPassword = "password123"

val testUserEntity = UserEntity(
    id = testUserID,
    email = testEmail,
    password = testHash,
    salt = testSalt,
    name = "Test User",
    specialDate = "",
    meetings = emptyList()
)