package repository

import models.RefreshTokenEntity

interface RefreshTokenRepository {
    suspend fun saveRefreshToken(refreshToken: RefreshTokenEntity): Boolean
    suspend fun getEntityByToken(token: String): RefreshTokenEntity?
    suspend fun revokeToken(token: String): Boolean
}