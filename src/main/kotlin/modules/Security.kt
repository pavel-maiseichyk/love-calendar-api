package modules

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject
import security.token.TokenConfig

fun Application.configureSecurity() {
    val tokenConfig by inject<TokenConfig>()

    authentication {
        jwt {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(tokenConfig.secret))
                    .withAudience(tokenConfig.audience)
                    .withIssuer(tokenConfig.issuer)
                    .build()
            )
            validate { credential ->
                val expiresAt = credential.payload.expiresAt?.time
                if (expiresAt != null && expiresAt < System.currentTimeMillis()) {
                    null
                } else if (credential.payload.audience.contains(tokenConfig.audience) &&
                    credential.payload.getClaim("type").asString() == "access"
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}