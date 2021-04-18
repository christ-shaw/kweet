package com.xzb.data

import io.ktor.locations.*
@KtorExperimentalLocationsAPI
@Location("/logout")
class Logout()

@KtorExperimentalLocationsAPI
@Location(path = "/login")
data class Login(val userId:String="",val error:String="")

