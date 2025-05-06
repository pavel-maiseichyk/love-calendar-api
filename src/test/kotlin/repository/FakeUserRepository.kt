package repository

import mapper.toUser
import model.user.User
import model.user.UserEntity

class FakeUserRepository : UserRepository {

    var users = mutableListOf<UserEntity>()
    var shouldSucceed: Boolean = true

    override suspend fun getUsers(): List<User> {
        return users.map { it.toUser() }
    }

    override suspend fun getUserByID(userID: String): User? {
        return users.find { it.id == userID }?.toUser()
    }

    override suspend fun getUserEntityByEmail(email: String): UserEntity? {
        return users.find { it.email == email }
    }

    override suspend fun addUserEntity(userEntity: UserEntity): String? {
        users.add(userEntity)
        return if (shouldSucceed) userEntity.id else null
    }

    override suspend fun updateUser(updatedUser: User): Boolean {
        users.map { user ->
            if (updatedUser.id == user.id) updatedUser else user
        }
        return shouldSucceed
    }

    override suspend fun removeUser(userID: String): Boolean {
        users.removeIf { it.id == userID }
        return shouldSucceed
    }
}