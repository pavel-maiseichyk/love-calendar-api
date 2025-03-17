package domain.models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class User(
    val id: String? = ObjectId().toString(),
    val name: String,
    val email: String,
    val specialDate: String,
    val meetings: List<String>
)