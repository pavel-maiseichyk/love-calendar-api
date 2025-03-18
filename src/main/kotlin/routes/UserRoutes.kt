package routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.buildJsonObject
import models.ErrorResponse
import models.SuccessResponse
import models.User
import repository.UserRepository

fun Route.userRoutes(repository: UserRepository) {
    authenticate {
        route("/users/{id}") {
            get {
                val userID = call.parameters["id"] ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(message = "Missing ID.")
                )

                val user = repository.getUserByID(userID = userID)
                call.respond(
                    status = HttpStatusCode.OK,
                    message = buildJsonObject {
                        "success" to true
                        "user" to user
                    },
                )
            }
            put {
                val userID = call.parameters["id"] ?: return@put call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(message = "Missing ID.")
                )
                val updatedUser = call.receive<User>()

                if (userID != updatedUser.id) return@put call.respond(
                    status = HttpStatusCode.Forbidden,
                    message = ErrorResponse(message = "Forbidden to update this user.")
                )

                val success = repository.updateUser(updatedUser)
                if (success) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = SuccessResponse
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = ErrorResponse(message = "Unable to update user.")
                    )
                }
            }
            delete {
                val userID = call.parameters["id"] ?: return@delete call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(message = "Missing ID.")
                )

                val success = repository.removeUser(userID)
                if (success) {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = SuccessResponse
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = ErrorResponse(message = "Unable to delete user.")
                    )
                }
            }
        }
    }
}

//        route("/users") {
//            get {
//                val users = repository.getUsers()
//                call.respond(status = HttpStatusCode.OK, mapOf("users" to users))
//            }
//        }