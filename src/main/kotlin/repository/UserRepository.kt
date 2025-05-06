package repository

import model.user.User
import model.user.UserEntity

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserByID(userID: String): User?
    suspend fun getUserEntityByEmail(email: String): UserEntity?
    suspend fun addUserEntity(userEntity: UserEntity): String?
    suspend fun updateUser(updatedUser: User): Boolean
    suspend fun removeUser(userID: String): Boolean
}