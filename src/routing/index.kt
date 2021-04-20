package com.xzb.routing

import com.xzb.datasource.DAOFacade
import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import session.KweetSession

@Location("/")
class Index()

fun Route.index(dao : DAOFacade)
{
    get<Index> {
        //  jump to the user info page if there is a kweet session carried
        val user = call.sessions.get<KweetSession>()?.let { dao.user(it.userId) }
        val top= dao.top(10).map { dao.getKweet(it) }
        val latest = dao.latest(10).map { dao.getKweet(it) }

          call.respond(mapOf("top" to top ,"latest" to latest))

    }
}
