import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import modules.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureLogging()
    configureKoin()
    configureStatusPages()
    configureSecurity()
    configureSerialization()
    configureRouting()
}