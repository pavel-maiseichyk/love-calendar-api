package routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.ErrorResponse
import models.SuccessResponse
import models.User
import repository.UserRepository

fun Route.userRoutes(repository: UserRepository) {
    authenticate {
        route("/users") {
            get {
                val userID = call.principal<JWTPrincipal>()?.get("userID") ?: return@get call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorResponse(message = "Missing ID.")
                )

                val user = repository.getUserByID(userID = userID)
                if (user == null) {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        message = ErrorResponse(message = "User with ID $userID not found.")
                    )
                } else {
                    call.respond(
                        status = HttpStatusCode.OK,
                        message = SuccessResponse(user = user),
                    )
                }
            }

            put {
                val updatedUser = call.receive<User>()

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
                val userID = call.principal<JWTPrincipal>()?.get("userID") ?: return@delete call.respond(
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