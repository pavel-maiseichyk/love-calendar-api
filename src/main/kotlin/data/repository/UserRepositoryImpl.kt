package data.repository

import domain.models.User
import domain.repository.UserRepository
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserRepositoryImpl(db: CoroutineDatabase) : UserRepository {
    private val users: CoroutineCollection<User> = db.getCollection()

    override suspend fun getUsers(): List<User> {
        return users.find().toList()
    }

    override suspend fun getUserByID(userID: String): User? {
        return users.findOne(User::id eq userID)
    }

    override suspend fun addUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun updateUser(updatedUser: User): Boolean {
        return users.replaceOne(User::id eq updatedUser.id, updatedUser).wasAcknowledged()
    }

    override suspend fun removeUser(userID: String): Boolean {
        return users.deleteOne(User::id eq userID).wasAcknowledged()
    }
}