package models

import io.ktor.http.HttpStatusCode

sealed class ApiException(val status: HttpStatusCode, override val message: String) : Exception(message) {
    class UnauthorizedException(message: String = "Unauthorized.") : ApiException(HttpStatusCode.Unauthorized, message)
    class NotFoundException(message: String) : ApiException(HttpStatusCode.NotFound, message)
    class ValidationException(message: String) : ApiException(HttpStatusCode.BadRequest, message)
    class BadRequestException(message: String) : ApiException(HttpStatusCode.BadRequest, message)
    class ConflictException(message: String) : ApiException(HttpStatusCode.Conflict, message)
    class ForbiddenException(message: String = "Access denied.") : ApiException(HttpStatusCode.Forbidden, message)
    class InternalServerException(message: String = "Internal server error.") : ApiException(HttpStatusCode.InternalServerError, message)
}