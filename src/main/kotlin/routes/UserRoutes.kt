package routes

import domain.models.User
import domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(repository: UserRepository) {

    route("/users") {
        get {
            val users = repository.getUsers()
            call.respond(status = HttpStatusCode.OK, mapOf("users" to users))
        }
        post {
            val user = call.receive<User>()

            val success = repository.addUser(user)
            if (success) {
                call.respond(status = HttpStatusCode.OK, mapOf("success" to true))
            } else {
                call.respond(status = HttpStatusCode.InternalServerError, mapOf("error" to "Unable to add user."))
            }
        }
    }

    route("/users/{id}") {
        get {
            val userID = call.parameters["id"] ?: return@get call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("error" to "Missing ID.")
            )

            val user = repository.getUserByID(userID = userID)
            call.respond(status = HttpStatusCode.OK, mapOf("user" to user))
        }
        put {
            val userID = call.parameters["id"] ?: return@put call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("error" to "Missing ID.")
            )
            val updatedUser = call.receive<User>()

            if (userID != updatedUser.id) return@put call.respond(
                status = HttpStatusCode.Forbidden,
                mapOf("error" to "Forbidden to update this user.")
            )

            val success = repository.updateUser(updatedUser)
            if (success) {
                call.respond(status = HttpStatusCode.OK, mapOf("success" to true))
            } else {
                call.respond(status = HttpStatusCode.InternalServerError, mapOf("error" to "Unable to update user."))
            }
        }
        delete {
            val userID = call.parameters["id"] ?: return@delete call.respond(
                status = HttpStatusCode.BadRequest,
                mapOf("error" to "Missing ID.")
            )

            val success = repository.removeUser(userID)
            if (success) {
                call.respond(status = HttpStatusCode.OK, mapOf("success" to true))
            } else {
                call.respond(status = HttpStatusCode.InternalServerError, mapOf("error" to "Unable to delete user."))
            }
        }
    }
}