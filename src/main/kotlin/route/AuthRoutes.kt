package route

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.core.ApiException
import model.auth.RefreshTokenEntity
import model.user.UserEntity
import model.auth.AuthRequest
import model.auth.RefreshTokenRequest
import model.auth.TokenResponse
import model.security.SaltedHash
import model.security.TokenClaim
import model.security.TokenConfig
import repository.RefreshTokenRepository
import repository.UserRepository
import security.hashing.HashingService
import security.token.TokenService

fun Route.authRoutes(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userRepository: UserRepository,
    refreshTokenRepository: RefreshTokenRepository
) {
    post("/sign_up") {
        val request = call.receive<AuthRequest>()

        val emailExists = userRepository.getUserEntityByEmail(email = request.email) != null
        if (emailExists) throw ApiException.UserAlreadyExists()

        validateEmail(request.email)
        validatePassword(request.password)

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = UserEntity(
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            name = "",
            specialDate = "",
            meetings = emptyList()
        )

        val userID =
            userRepository.addUserEntity(user) ?: throw ApiException.InternalServerError("Failed to add user.")

        val (accessToken, refreshToken) = generateAndStoreTokens(
            tokenService = tokenService,
            tokenConfig = tokenConfig,
            userID = userID,
            refreshTokenRepository = refreshTokenRepository
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        )
    }

    post("/sign_in") {
        val request = call.receive<AuthRequest>()

        val userEntity = userRepository.getUserEntityByEmail(email = request.email)
            ?: throw ApiException.UserNotFound()

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                salt = userEntity.salt,
                hash = userEntity.password
            )
        )
        if (!isValidPassword) throw ApiException.InvalidPassword()

        val (accessToken, refreshToken) = generateAndStoreTokens(
            tokenService = tokenService,
            tokenConfig = tokenConfig,
            userID = userEntity.id,
            refreshTokenRepository = refreshTokenRepository
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        )
    }

    post("/sign_out") {
        val request = call.receive<RefreshTokenRequest>()
        val refreshToken = request.refreshToken

        val success = refreshTokenRepository.revokeToken(refreshToken)
        if (!success) throw ApiException.InvalidRefreshToken()

        call.respond(HttpStatusCode.OK)
    }

    post("/refresh") {
        val request = call.receive<RefreshTokenRequest>()
        val refreshToken = request.refreshToken

        val tokenValidation = tokenService.verifyToken(refreshToken, tokenConfig)
        if (!tokenValidation.isValid) {
            throw ApiException.InvalidRefreshToken()
        }

        val tokenEntity = refreshTokenRepository.getEntityByToken(refreshToken)
            ?: throw ApiException.InvalidRefreshToken("Token not found.")

        if (tokenEntity.isRevoked || tokenEntity.expiresAt < System.currentTimeMillis()) {
            throw ApiException.InvalidRefreshToken("Token has been revoked or expired.")
        }

        val success = refreshTokenRepository.revokeToken(refreshToken)
        if (!success) throw ApiException.InternalServerError("Unable to remove refresh token.")

        val userID = tokenValidation.claims["userID"]
            ?: throw ApiException.InvalidRefreshToken("Invalid token payload.")

        val (newAccessToken, newRefreshToken) = generateAndStoreTokens(
            tokenService, tokenConfig, userID, refreshTokenRepository
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = TokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        )
    }
}

private fun validateEmail(email: String) {
    val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()
    if (!email.matches(emailRegex)) throw ApiException.InvalidEmailFormat()
}

private fun validatePassword(password: String) {
    if (password.length < 8) throw ApiException.InvalidPasswordLength()
}

private suspend fun generateAndStoreTokens(
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userID: String,
    refreshTokenRepository: RefreshTokenRepository
): Pair<String, String> {
    val accessToken = tokenService.generateAccessToken(
        config = tokenConfig,
        TokenClaim("userID", userID)
    )

    val refreshToken = tokenService.generateRefreshToken(
        config = tokenConfig,
        TokenClaim("userID", userID)
    )
    val success = saveRefreshToken(
        userID = userID,
        refreshToken = refreshToken,
        tokenConfig = tokenConfig,
        refreshTokenRepository = refreshTokenRepository
    )
    if (!success) throw ApiException.InternalServerError("Couldn't save refresh token.")

    return accessToken to refreshToken
}

private suspend fun saveRefreshToken(
    userID: String,
    refreshToken: String,
    tokenConfig: TokenConfig,
    refreshTokenRepository: RefreshTokenRepository
): Boolean {
    val existingToken = refreshTokenRepository.getEntityByToken(refreshToken)
    if (existingToken != null) {
        throw ApiException.InvalidRefreshToken("Token collision detected.")
    }
    val refreshTokenEntity = RefreshTokenEntity(
        userID = userID,
        token = refreshToken,
        expiresAt = System.currentTimeMillis() + tokenConfig.refreshTokenExpiration
    )
    return refreshTokenRepository.saveRefreshToken(refreshTokenEntity)
}