package mappers

import models.User
import models.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id,
        email = email,
        name = name,
        specialDate = specialDate,
        meetings = meetings
    )
}

fun User.toEntity(
    password: String,
    salt: String
): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        password = password,
        salt = salt,
        name = name,
        specialDate = specialDate,
        meetings = meetings
    )
}