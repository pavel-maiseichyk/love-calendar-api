package domain.repository

import domain.models.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserByID(userID: String): User?
    suspend fun addUser(user: User): Boolean
    suspend fun updateUser(updatedUser: User): Boolean
    suspend fun removeUser(userID: String): Boolean
}