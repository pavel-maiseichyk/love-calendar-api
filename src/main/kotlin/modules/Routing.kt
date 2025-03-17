package modules

import routes.userRoutes
import domain.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    routing {
        userRoutes(userRepository)
    }
}
