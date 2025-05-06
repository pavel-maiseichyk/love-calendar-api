package repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import model.auth.RefreshTokenEntity

class RefreshTokenRepositoryImpl(
    private val refreshTokens: MongoCollection<RefreshTokenEntity>
) : RefreshTokenRepository {

    override suspend fun saveRefreshToken(refreshToken: RefreshTokenEntity): Boolean {
        return refreshTokens.insertOne(refreshToken).wasAcknowledged()
    }

    override suspend fun getEntityByToken(token: String): RefreshTokenEntity? {
        val filter = eq(RefreshTokenEntity::token.name, token)
        return refreshTokens.find(filter).firstOrNull()
    }

    override suspend fun revokeToken(token: String): Boolean {
        val filter = eq(RefreshTokenEntity::token.name, token)
        return refreshTokens.updateOne(
            filter = filter,
            update = Updates.set(RefreshTokenEntity::isRevoked.name, true)
        ).wasAcknowledged()
    }
}