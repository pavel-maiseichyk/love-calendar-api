package util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import model.security.TokenConfig
import model.security.testConfig
import module.configureStatusPages
import repository.RefreshTokenRepository
import repository.UserRepository
import route.authRoutes
import route.userRoutes
import security.hashing.HashingService
import security.token.TokenService
import java.util.*

open class BaseTestWithoutKoin {
    protected fun Application.configureTestApplication() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        configureStatusPages()
        authentication {
            jwt {
                verifier(
                    verifier = JWT
                        .require(Algorithm.HMAC256(testConfig.secret))
                        .withAudience(testConfig.audience)
                        .withIssuer(testConfig.issuer)
                        .build()
                )
                validate { credential -> JWTPrincipal(credential.payload) }
            }
        }
    }

    protected fun testWithUserRoutes(
        repository: UserRepository,
        testBlock: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) = testApplication {
        application {
            configureTestApplication()
        }
        createContentNegotiationPlugin()
        routing { userRoutes(repository) }
        testBlock(client)
    }

    protected fun testWithAuthRoutes(
        hashingService: HashingService,
        tokenService: TokenService,
        tokenConfig: TokenConfig,
        userRepository: UserRepository,
        refreshTokenRepository: RefreshTokenRepository,
        testBlock: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) = testApplication {
        application {
            configureTestApplication()
        }
        createContentNegotiationPlugin()
        routing {
            authRoutes(
                hashingService = hashingService,
                tokenConfig = tokenConfig,
                tokenService = tokenService,
                userRepository = userRepository,
                refreshTokenRepository = refreshTokenRepository
            )
        }
        testBlock(client)
    }

    protected fun generateTestJWT(userID: String): String {
        return JWT.create()
            .withAudience(testConfig.audience)
            .withIssuer(testConfig.issuer)
            .withClaim("userID", userID)
            .withExpiresAt(Date(System.currentTimeMillis() + 60000L))
            .sign(Algorithm.HMAC256(testConfig.secret))
    }

    private fun ApplicationTestBuilder.createContentNegotiationPlugin(): HttpClient {
        return createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}