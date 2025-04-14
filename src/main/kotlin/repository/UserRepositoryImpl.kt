package repository

import mappers.toEntity
import mappers.toUser
import models.User
import models.UserEntity
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserRepositoryImpl(
    private val db: CoroutineDatabase
) : UserRepository {

    private val users: CoroutineCollection<UserEntity> = db.getCollection("users")

    override suspend fun getUsers(): List<User> {
        return users.find().toList().map { it.toUser() }
    }

    override suspend fun getUserByID(userID: String): User? {
        return users.findOne(UserEntity::id eq userID)?.toUser()
    }

    override suspend fun getUserEntityByEmail(email: String): UserEntity? {
        return users.findOne(UserEntity::email eq email)
    }

    override suspend fun addUserEntity(userEntity: UserEntity): String? {
        val success = users.insertOne(userEntity).wasAcknowledged()
        if (!success) return null

        return getUserEntityByEmail(userEntity.email)?.id ?: return null
    }

    override suspend fun updateUser(updatedUser: User): Boolean {
        val oldUserEntity = users.findOne(UserEntity::id eq updatedUser.id) ?: return false
        val updatedUserEntity = updatedUser.toEntity(password = oldUserEntity.password, salt = oldUserEntity.salt)
        return users.replaceOne(UserEntity::id eq updatedUserEntity.id, updatedUserEntity).wasAcknowledged()
    }

    override suspend fun removeUser(userID: String): Boolean {
        return users.deleteOne(UserEntity::id eq userID).wasAcknowledged()
    }
}