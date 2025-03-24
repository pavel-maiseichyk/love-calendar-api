package modules

import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import models.ErrorResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<NoTransformationFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(message = cause.message)
            )
        }
    }
}
