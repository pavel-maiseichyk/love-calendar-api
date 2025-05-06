package module

import io.ktor.http.*
import model.core.ApiException
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import model.core.ErrorResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                status = cause.status,
                message = ErrorResponse(
                    errorCode = cause.errorCode.name,
                    message = cause.message
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    errorCode = ApiException.ErrorCode.INTERNAL_SERVER_ERROR.name,
                    message = cause.message ?: "Internal server error."
                )
            )
        }
    }
}
