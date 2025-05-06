package module

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import repository.RefreshTokenRepository
import repository.UserRepository
import route.authRoutes
import route.userRoutes
import security.hashing.HashingService
import model.security.TokenConfig
import security.token.TokenService

fun Application.configureRouting() {
    val userRepository by inject<UserRepository>()
    val refreshTokenRepository by inject<RefreshTokenRepository>()
    val hashingService by inject<HashingService>()
    val tokenService by inject<TokenService>()
    val tokenConfig by inject<TokenConfig>()

    routing {
        authRoutes(
            hashingService = hashingService,
            tokenService = tokenService,
            tokenConfig = tokenConfig,
            userRepository = userRepository,
            refreshTokenRepository = refreshTokenRepository
        )
        userRoutes(
            repository = userRepository
        )
    }
}
