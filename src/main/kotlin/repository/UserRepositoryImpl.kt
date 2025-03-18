package repository

import mappers.toEntity
import mappers.toUser
import models.User
import models.UserEntity
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserRepositoryImpl(db: CoroutineDatabase) : UserRepository {
    private val users: CoroutineCollection<UserEntity> = db.getCollection()

    override suspend fun getUsers(): List<User> {
        return users.find().toList().map { it.toUser() }
    }

    override suspend fun getUserByID(userID: String): User? {
        return users.findOne(UserEntity::id eq userID)?.toUser()
    }

    override suspend fun getUserByEmail(email: String): User? {
        return users.findOne(UserEntity::email eq email)?.toUser()
    }

    override suspend fun doesUserExist(email: String): Boolean {
        return users.findOne(UserEntity::email eq email) != null
    }

    override suspend fun addUser(user: UserEntity): Boolean {
        return users.insertOne(user).wasAcknowledged()
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