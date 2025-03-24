package com.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import repository.UserRepository
import routes.authRoutes
import routes.userRoutes
import security.hashing.HashingService
import security.token.TokenConfig
import security.token.TokenService

abstract class BaseRoutesTest {
    protected val testSecret = "test-secret"
    protected val audience = "com.pm.love_calendar"
    protected val issuer = "https://lcal.com"

    protected fun Application.configureTestApplication() {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        authentication {
            jwt {
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(testSecret))
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(audience)) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
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

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            userRoutes(repository)
        }

        testBlock(client)
    }

    protected fun testWithAuthRoutes(
        hashingService: HashingService,
        tokenService: TokenService,
        tokenConfig: TokenConfig,
        userRepository: UserRepository,
        testBlock: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) = testApplication {
        application {
            configureTestApplication()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        routing {
            authRoutes(
                hashingService = hashingService,
                tokenService = tokenService,
                tokenConfig = tokenConfig,
                userRepository = userRepository
            )
        }

        testBlock(client)
    }

    protected fun generateTestToken(userID: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userID", userID)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 60000)) // 1 минута
            .sign(Algorithm.HMAC256(testSecret))
    }

    protected fun generateFailedTestToken(userID: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 60000)) // 1 минута
            .sign(Algorithm.HMAC256(testSecret))
    }
}