package model.core

import io.ktor.http.HttpStatusCode

sealed class ApiException(
    val status: HttpStatusCode,
    val errorCode: ErrorCode,
    override val message: String
) : Exception(message) {

    enum class ErrorCode {
        USER_ALREADY_EXISTS,
        INVALID_EMAIL_FORMAT,
        INVALID_PASSWORD_LENGTH,
        USER_NOT_FOUND,
        INVALID_PASSWORD,
        INVALID_REFRESH_TOKEN,
        INVALID_ACCESS_TOKEN,
        INTERNAL_SERVER_ERROR
    }

    class UserAlreadyExists(message: String = "User already exists.") :
        ApiException(HttpStatusCode.Conflict, ErrorCode.USER_ALREADY_EXISTS, message)

    class InvalidEmailFormat(message: String = "Email format is invalid.") :
        ApiException(HttpStatusCode.BadRequest, ErrorCode.INVALID_EMAIL_FORMAT, message)

    class InvalidPasswordLength(message: String = "Password length is invalid.") :
        ApiException(HttpStatusCode.BadRequest, ErrorCode.INVALID_PASSWORD_LENGTH, message)

    class UserNotFound(message: String = "User not found.") :
        ApiException(HttpStatusCode.NotFound, ErrorCode.USER_NOT_FOUND, message)

    class InvalidPassword(message: String = "Invalid password.") :
        ApiException(HttpStatusCode.Forbidden, ErrorCode.INVALID_PASSWORD, message)

    class InvalidRefreshToken(message: String = "Invalid refresh token.") :
        ApiException(HttpStatusCode.Unauthorized, ErrorCode.INVALID_REFRESH_TOKEN, message)

    class InvalidAccessToken(message: String = "Invalid access token.") :
        ApiException(HttpStatusCode.Unauthorized, ErrorCode.INVALID_ACCESS_TOKEN, message)

    class InternalServerError(message: String = "Internal server error.") :
        ApiException(HttpStatusCode.InternalServerError, ErrorCode.INTERNAL_SERVER_ERROR, message)
}