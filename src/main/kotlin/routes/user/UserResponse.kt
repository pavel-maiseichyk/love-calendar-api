package routes.user

import kotlinx.serialization.Serializable
import models.User

@Serializable
data class UserResponse(
    val user: User
)
