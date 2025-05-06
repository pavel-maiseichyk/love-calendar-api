package model.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val specialDate: String,
    val meetings: List<String>
)