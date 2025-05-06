package model.user

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class UserEntity(
    @SerialName("_id")
    @Contextual val id: String = ObjectId().toHexString(),
    val email: String,
    val password: String,
    val salt: String,

    val name: String,
    val specialDate: String,
    val meetings: List<String>
)