package di

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import model.auth.RefreshTokenEntity
import model.user.UserEntity
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import repository.RefreshTokenRepository
import repository.RefreshTokenRepositoryImpl
import repository.UserRepository
import repository.UserRepositoryImpl
import security.hashing.HashingService
import security.hashing.SHA256HashingService
import security.token.JWTTokenService
import model.security.TokenConfig
import security.token.TokenService
import util.Constants.DATABASE_NAME
import util.Constants.MONGO_CONNECTION_STRING
import util.Constants.REFRESH_TOKENS_COLLECTION_NAME
import util.Constants.USERS_COLLECTION_NAME

val koinModule = module {
    single<MongoDatabase> {
        val client = MongoClient.create(MONGO_CONNECTION_STRING)
        client.getDatabase(databaseName = DATABASE_NAME)
    }
    single<MongoCollection<UserEntity>>(named("users_collection")) {
        get<MongoDatabase>().getCollection<UserEntity>(USERS_COLLECTION_NAME)
    }
    single<MongoCollection<RefreshTokenEntity>>(named("tokens_collection")) {
        get<MongoDatabase>().getCollection<RefreshTokenEntity>(REFRESH_TOKENS_COLLECTION_NAME)
    }
    single<UserRepository> {
        UserRepositoryImpl(get(named("users_collection")))
    }
    single<RefreshTokenRepository> {
        RefreshTokenRepositoryImpl(get(named("tokens_collection")))
    }
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