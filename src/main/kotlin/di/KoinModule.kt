package di

import models.RefreshTokenEntity
import models.UserEntity
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import repository.RefreshTokenRepository
import repository.RefreshTokenRepositoryImpl
import repository.UserRepository
import repository.UserRepositoryImpl
import security.hashing.HashingService
import security.hashing.SHA256HashingService
import security.token.JWTTokenService
import security.token.TokenConfig
import security.token.TokenService
import utils.Constants.DATABASE_NAME
import utils.Constants.MONGO_CONNECTION_STRING

val koinModule = module {
    single<CoroutineDatabase> {
        KMongo.createClient(MONGO_CONNECTION_STRING)
            .coroutine
            .getDatabase(DATABASE_NAME)
    }
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    singleOf(::RefreshTokenRepositoryImpl).bind<RefreshTokenRepository>()
    singleOf(::JWTTokenService).bind<TokenService>()
    singleOf(::SHA256HashingService).bind<HashingService>()
    single<TokenConfig> {
        TokenConfig(
            issuer = System.getenv("ISSUER") ?: throw IllegalStateException("Missing ISSUER."),
            audience = System.getenv("AUDIENCE") ?: throw IllegalStateException("Missing AUDIENCE."),
            secret = System.getenv("JWT_SECRET") ?: throw IllegalStateException("Missing JWT_SECRET.")
        )
    }
}