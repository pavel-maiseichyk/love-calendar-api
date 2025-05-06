package model.core

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val errorCode: String,
    val message: String
)