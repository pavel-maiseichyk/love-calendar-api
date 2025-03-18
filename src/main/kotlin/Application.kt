import modules.configureKoin
import modules.configureRouting
import modules.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import modules.configureSecurity

fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureKoin()
    configureSecurity()
    configureSerialization()
    configureRouting()
}