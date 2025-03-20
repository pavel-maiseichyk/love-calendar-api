package models

import kotlinx.serialization.Serializable

@Serializable
data class SuccessResponse(
    val success: Boolean = true,
    val user: User? = null
)

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String
)