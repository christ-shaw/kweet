package com.xzb.datasource

import com.xzb.entity.Kweets
import com.xzb.entity.Users
import com.xzb.entity.Users.displayName
import com.xzb.entity.Users.email
import com.xzb.entity.Users.passwordHash
import com.xzb.model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.Closeable
import java.io.File

interface DAOFacade : Closeable {
    fun init()

    /**
     * Creates a new [user] in the database from its object [User] representation.
     */
    fun createUser(user: User)

}

class DAOFacadeDatabase(

) : DAOFacade {
    val db = Database.connect(DatabaseFactory.create())


    override fun init() {
        // Create the used tables
        transaction(db) {
            SchemaUtils.create(Users, Kweets)
        }
    }

    override fun createUser(user: User) {
        transaction(db) {
            Users.insert {
                it[id] = user.userId
                it[displayName] = user.displayName
                it[email] = user.email
                it[passwordHash] = user.passwordHash
            }
        }
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}