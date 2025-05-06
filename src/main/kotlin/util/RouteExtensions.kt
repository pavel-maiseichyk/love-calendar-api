package util

import model.core.ApiException
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.getUserID(): String {
    return principal<JWTPrincipal>()?.get("userID")
        ?: throw ApiException.InvalidAccessToken()
}