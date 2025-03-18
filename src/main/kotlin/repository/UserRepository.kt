package repository

import models.User
import models.UserEntity

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserByID(userID: String): User?
    suspend fun getUserByEmail(email: String): UserEntity?
    suspend fun doesUserExist(email: String): Boolean
    suspend fun addUser(user: UserEntity): Boolean
    suspend fun updateUser(updatedUser: User): Boolean
    suspend fun removeUser(userID: String): Boolean
}