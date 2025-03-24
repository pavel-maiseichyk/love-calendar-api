package di

import models.UserEntity
import repository.UserRepositoryImpl
import repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import security.hashing.HashingService
import security.hashing.SHA256HashingService
import security.token.JWTTokenService
import security.token.TokenConfig
import security.token.TokenService
import utils.Constants.DATABASE_NAME
import utils.Constants.MONGO_CONNECTION_STRING

val koinModule = module {
    single<CoroutineDatabase> {
        KMongo
            .createClient(MONGO_CONNECTION_STRING)
            .coroutine
            .getDatabase(DATABASE_NAME)
    }
    single<CoroutineCollection<UserEntity>> {
        get<CoroutineDatabase>().getCollection<UserEntity>()
    }
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    singleOf(::JWTTokenService).bind<TokenService>()
    singleOf(::SHA256HashingService).bind<HashingService>()
    single<TokenConfig> {
        TokenConfig(
            issuer = System.getenv("ISSUER") ?: throw IllegalStateException("Missing ISSUER."),
            audience = System.getenv("AUDIENCE") ?: throw IllegalStateException("Missing AUDIENCE."),
            expiresIn = 365L * 24L * 60L * 60L * 1000L,
            secret = System.getenv("JWT_SECRET") ?: throw IllegalStateException("Missing JWT_SECRET.")
        )
    }
}