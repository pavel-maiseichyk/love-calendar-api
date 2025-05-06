package mapper

import model.user.User
import model.user.UserEntity

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