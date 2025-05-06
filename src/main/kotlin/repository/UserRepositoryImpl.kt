package repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import mapper.toEntity
import mapper.toUser
import model.user.User
import model.user.UserEntity

class UserRepositoryImpl(
    private val users: MongoCollection<UserEntity>
) : UserRepository {

    override suspend fun getUsers(): List<User> {
        return users.find().toList().map { it.toUser() }
    }

    override suspend fun getUserByID(userID: String): User? {
        val filter = eq(UserEntity::id.name, userID)
        return users.find(filter).firstOrNull()?.toUser()
    }

    override suspend fun getUserEntityByEmail(email: String): UserEntity? {
        val filter = eq(UserEntity::email.name, email)
        return users.find(filter).firstOrNull()
    }

    override suspend fun addUserEntity(userEntity: UserEntity): String? {
        val success = users.insertOne(userEntity).wasAcknowledged()
        if (!success) return null

        return getUserEntityByEmail(userEntity.email)?.id
    }

    override suspend fun updateUser(updatedUser: User): Boolean {
        val filter = eq(UserEntity::id.name, updatedUser.id)
        val oldUserEntity = users.find(filter).firstOrNull() ?: return false
        val updatedUserEntity = updatedUser.toEntity(password = oldUserEntity.password, salt = oldUserEntity.salt)

        return users.replaceOne(filter, updatedUserEntity).wasAcknowledged()
    }

    override suspend fun removeUser(userID: String): Boolean {
        val filter = eq(UserEntity::id.name, userID)
        return users.deleteOne(filter).wasAcknowledged()
    }
}