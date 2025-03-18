package modules

import routes.userRoutes
import repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import routes.authRoutes
import security.hashing.HashingService
import security.token.TokenConfig
import security.token.TokenService

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    val hashingService by inject<HashingService>()
    val tokenService by inject<TokenService>()
    val tokenConfig by inject<TokenConfig>()

    routing {
        authRoutes(
            hashingService = hashingService,
            tokenService = tokenService,
            tokenConfig = tokenConfig,
            userRepository = userRepository
        )
        userRoutes(userRepository)
    }
}
