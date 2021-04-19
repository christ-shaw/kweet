package com.xzb.routing

import com.xzb.datasource.DAOFacade
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import session.KweetSession

@Location("/user/{user}")
data class UserPage(val user: String)


fun Route.userPage(dao: DAOFacade) {
    get<UserPage>{
        val user = call.sessions.get<KweetSession>()?.let { it -> dao.user(it.userId) }
        val pageUser = dao.user(it.user)
        if (pageUser == null) {
            call.respond(HttpStatusCode.NotFound.description("User ${it.user} doesn't exist"))
        } else {
            call.respond(HttpStatusCode.OK,message ="User ${user?.displayName}" )
        }

    }

}