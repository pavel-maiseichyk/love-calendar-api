package utils

import models.ApiException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.pipeline.*

fun ApplicationCall.getUserID(): String {
    return principal<JWTPrincipal>()?.get("userID")
        ?: throw ApiException.UnauthorizedException("Invalid or missing token.")
}