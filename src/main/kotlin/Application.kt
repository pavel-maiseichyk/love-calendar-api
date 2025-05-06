import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import module.*

fun main() {
    embeddedServer(Netty, port = 8081) {
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