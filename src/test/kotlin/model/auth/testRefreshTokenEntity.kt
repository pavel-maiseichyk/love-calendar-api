package model.auth

import model.user.testUserID

const val testAccessToken = "test-access-token"
const val testRefreshToken = "test-refresh-token"

val testRefreshTokenEntity = RefreshTokenEntity(
    userID = testUserID,
    token = testRefreshToken,
    expiresAt = System.currentTimeMillis() + 3600000L,
    isRevoked = false
)