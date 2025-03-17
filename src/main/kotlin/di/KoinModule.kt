package di

import data.repository.UserRepositoryImpl
import domain.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import utils.Constants.DATABASE_NAME
import utils.Constants.MONGO_CONNECTION_STRING

val koinModule = module {
    single<CoroutineDatabase> {
        KMongo
            .createClient(MONGO_CONNECTION_STRING)
            .coroutine
            .getDatabase(DATABASE_NAME)
    }
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
}