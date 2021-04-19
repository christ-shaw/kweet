package com.xzb.datasource

import com.xzb.entity.Kweets
import com.xzb.entity.Users
import com.xzb.model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.Closeable

interface DAOFacade : Closeable {
    fun init()

    /**
     * Creates a new [user] in the database from its object [User] representation.
     */
    fun createUser(user: User)


    fun user(userId: String, hash: String? = null): User?

    fun userByEmail(email:String):User?

}

class DAOFacadeDatabase( ) : DAOFacade {
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

    override fun user(userId: String, hash: String?): User? {
       return transaction(db)
        {
            Users.select{Users.id.eq(userId)}
                .mapNotNull {
                    if(hash == null || it[Users.passwordHash] == hash)
                    {
                        User(userId,it[Users.email],it[Users.displayName],it[Users.passwordHash])
                    }
                    else
                    {
                        null
                    }
                }.singleOrNull()
        }
    }

    override fun userByEmail(email: String): User? {
      return transaction(db)
      {
          Users.select{
              Users.email.eq(email)
          }.mapNotNull { User(it[Users.id],email,it[Users.displayName],passwordHash = it[Users.passwordHash]) }
              .singleOrNull()
      }
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}