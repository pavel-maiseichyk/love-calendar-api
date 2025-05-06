package model.auth

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class RefreshTokenEntity(
    @SerialName("_id")
    @Contextual val id: String = ObjectId().toHexString(),
    val userID: String,
    val token: String,
    val expiresAt: Long,
    val isRevoked: Boolean = false
)