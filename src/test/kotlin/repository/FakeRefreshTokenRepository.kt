package repository

import model.auth.RefreshTokenEntity
import model.auth.testRefreshTokenEntity

class FakeRefreshTokenRepository : RefreshTokenRepository {

    private var getEntityTimesCalled: Int = 0
    var tokens = mutableListOf<RefreshTokenEntity>()
    var shouldSave: Boolean = true
    var shouldDelete: Boolean = true
    var shouldGet2ndTime: Boolean = true

    override suspend fun saveRefreshToken(refreshToken: RefreshTokenEntity): Boolean {
        tokens.add(refreshToken)
        return shouldSave
    }

    override suspend fun getEntityByToken(token: String): RefreshTokenEntity? {
        getEntityTimesCalled += 1
        println(getEntityTimesCalled)
        if (getEntityTimesCalled == 2 && !shouldGet2ndTime) return testRefreshTokenEntity
        return tokens.find { it.token == token }
    }

    override suspend fun revokeToken(token: String): Boolean {
        tokens.removeIf { it.token == token }
        return shouldDelete
    }
}