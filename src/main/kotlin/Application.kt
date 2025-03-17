import modules.configureFrameworks
import modules.configureRouting
import modules.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureFrameworks()
    configureSerialization()
    configureRouting()
}