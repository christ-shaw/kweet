package com.xzb

import com.fasterxml.jackson.databind.*
import com.xzb.datasource.DAOFacade
import com.xzb.datasource.DAOFacadeDatabase
import com.xzb.routing.register
import com.xzb.routing.userPage
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.css.*
import kotlinx.html.*
import org.slf4j.event.*
import session.KweetSession
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
val dao :DAOFacade  = DAOFacadeDatabase()

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ConditionalHeaders)

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Sessions) {
        cookie<KweetSession>("LOGIN_SESSION"){
            val secretSignKey = hex("000102030405060708090a0b0c0d0e0f")
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
            cookie.path = "/"
            cookie.maxAgeInSeconds = 1000
        }
    }

    // 初始化数据库
   dao.init()
    val hashFunction = { s: String -> hash(s) }
    routing {
        register(dao,hashFunction)
        userPage(dao)
//
//        get("/") {
//
//            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
//        }
//
//        get("/html-dsl") {
//            call.respondHtml {
//                body {
//                    h1 { +"HTML" }
//                    ul {
//                        for (n in 1..10) {
//                            li { +"$n" }
//                        }
//                    }
//                }
//            }
//        }
//
//        get("/styles.css") {
//            call.respondCss {
//                body {
//                    backgroundColor = Color.red
//                }
//                p {
//                    fontSize = 2.em
//                }
//                rule("p.myclass") {
//                    color = Color.blue
//                }
//            }
//        }

        get<MyLocation> {
            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
        }
        // Register nested routes
        get<Type.Edit> {
            call.respondText("Inside $it")
        }
        get<Type.List> {
            call.respondText("Inside $it")
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@Location("/type/{name}") data class Type(val name: String) {
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}


suspend fun ApplicationCall.redirect(location: Any) {
    val host = request.host() ?: "localhost"
    val portSpec = request.port().let { if (it == 80) "" else ":$it" }
    val address = host + portSpec

    respondRedirect("http://$address${application.locations.href(location)}")
}
fun hash(password: String): String {
    val hashKey = hex("6819b57a326945c1968f45236589")
    val hmacKey = SecretKeySpec(hashKey, "HmacSHA1")
    val hmac = Mac.getInstance("HmacSHA1")
    hmac.init(hmacKey)
    return hex(hmac.doFinal(password.toByteArray(Charsets.UTF_8)))
}