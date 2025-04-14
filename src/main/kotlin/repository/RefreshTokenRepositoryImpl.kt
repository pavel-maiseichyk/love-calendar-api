package repository

import models.RefreshTokenEntity
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class RefreshTokenRepositoryImpl(
    private val db: CoroutineDatabase
) : RefreshTokenRepository {

    private val refreshTokens: CoroutineCollection<RefreshTokenEntity> = db.getCollection("refresh_tokens")

    override suspend fun saveRefreshToken(refreshToken: RefreshTokenEntity): Boolean {
        return refreshTokens.insertOne(refreshToken).wasAcknowledged()
    }

    override suspend fun getEntityByToken(token: String): RefreshTokenEntity? {
        return refreshTokens.findOne(RefreshTokenEntity::token eq token)
    }

    override suspend fun revokeToken(token: String): Boolean {
        val result = refreshTokens.updateOne(
            RefreshTokenEntity::token eq token,
            setValue(RefreshTokenEntity::isRevoked, true)
        )
        return result.wasAcknowledged()
    }
}