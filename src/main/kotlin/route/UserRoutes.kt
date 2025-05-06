package route

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.core.ApiException
import model.user.User
import model.user.UserResponse
import repository.UserRepository
import util.getUserID

fun Route.userRoutes(repository: UserRepository) {
    authenticate {
        route("/users") {
            get {
                val userID = call.getUserID()
                val user = repository.getUserByID(userID = userID) ?: throw ApiException.UserNotFound()

                call.respond(
                    status = HttpStatusCode.OK,
                    message = UserResponse(user)
                )
            }

            put {
                val updatedUser = call.receive<User>()

                val success = repository.updateUser(updatedUser)
                if (!success) throw ApiException.InternalServerError("Unable to update user.")

                call.respond(HttpStatusCode.OK)
            }

            delete {
                val userID = call.getUserID()

                val success = repository.removeUser(userID)
                if (!success) throw ApiException.InternalServerError("Unable to delete user.")

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}