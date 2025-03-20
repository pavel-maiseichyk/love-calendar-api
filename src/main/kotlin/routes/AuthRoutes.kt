package routes

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mappers.toUser
import models.*
import repository.UserRepository
import security.hashing.HashingService
import security.hashing.SaltedHash
import security.token.TokenClaim
import security.token.TokenConfig
import security.token.TokenService

fun Route.authRoutes(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userRepository: UserRepository
) {
    post("/sign_up") {
        val request = call.receiveNullable<AuthRequest>()
        if (request == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(message = "Bad request.")
            )
            return@post
        }

        val areFieldsBlank = request.email.isBlank() || request.password.isBlank()
        if (areFieldsBlank) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(message = "The fields were filled incorrectly.")
            )
            return@post
        }

        val doesUserExist = userRepository.doesUserExist(email = request.email)
        if (doesUserExist) {
            call.respond(
                status = HttpStatusCode.Conflict,
                message = ErrorResponse(message = "User with email ${request.email} already exists.")
            )
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = UserEntity(
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            name = "",
            specialDate = "",
            meetings = emptyList()
        )
        val wasAcknowledged = userRepository.addUser(user)
        if (!wasAcknowledged) {
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(message = "Failed to add user.")
            )
            return@post
        }

        call.respond(
            status = HttpStatusCode.OK,
            message = SuccessResponse()
        )
    }

    post("/sign_in") {
        val request = call.receiveNullable<AuthRequest>() ?: run {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(message = "Bad request.")
            )
            return@post
        }

        val userEntity = userRepository.getUserByEmail(email = request.email)
        if (userEntity == null) {
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(message = "User with email ${request.email} not found.")
            )
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                salt = userEntity.salt,
                hash = userEntity.password
            )
        )
        if (!isValidPassword) {
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = ErrorResponse(message = "Invalid password.")
            )
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userID",
                value = userEntity.id
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token,
                user = userEntity.toUser()
            )
        )
    }

    get("/unauthorized") {
        call.respond(
            status = HttpStatusCode.Unauthorized,
            message = ErrorResponse(message = "Not authorized.")
        )
    }

    authenticate {
        get("/authenticate") {
            call.respond(
                status = HttpStatusCode.OK,
                message = SuccessResponse()
            )
        }
    }
}