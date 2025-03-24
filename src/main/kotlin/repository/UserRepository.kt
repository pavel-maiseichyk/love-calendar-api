package repository

import models.User
import models.UserEntity

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserByID(userID: String): User?
    suspend fun getUserEntityByEmail(email: String): UserEntity?
    suspend fun addUserEntity(userEntity: UserEntity): Boolean
    suspend fun updateUser(updatedUser: User): Boolean
    suspend fun removeUser(userID: String): Boolean
}