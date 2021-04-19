package com.xzb.routing

import com.xzb.datasource.DAOFacade
import com.xzb.model.User
import com.xzb.redirect
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import session.KweetSession

@Location("/register")
data class Register(val userId: String = "", val displayName: String = "",
                    val email: String = "", val password : String ="",
                    val error: String = "")



fun Route.register(dao : DAOFacade, hashFunction: (String) -> String)
{
    /**
     * 注册路由
     */
    post<Register>{path ->
        // 查询用户是否有session
        val user = call.sessions.get<KweetSession>()?.let { dao.user(it.userId) }
        if (user != null) return@post call.redirect(UserPage(user.userId))

        else
        {

           // 判断各个字段是否为空
            val registration = call.receive<Register>()
        /*    if (registration.userId.isEmpty() ||
                    registration.displayName.isEmpty() ||
                    registration.email .isEmpty() ||
                        registration.password .isEmpty())
            {
                return@post call.redirect(path)
            }
*/
            val hash = hashFunction(registration.password)
            val newUser = User(registration.userId, registration.email, registration.displayName, hash)
            dao.createUser(newUser)


            call.sessions.set(KweetSession(newUser.userId))
            call.redirect(UserPage(newUser.userId))
        }


    }

    get<Register> {
        val user = call.sessions.get<KweetSession>()?.let { dao.user(it.userId) }
        if (user != null) {
            call.redirect(UserPage(user.userId))
        } else {
            call.respondText("nothing")
        }
    }
}