package com.xzb.datasource

import com.xzb.entity.Kweets
import com.xzb.entity.Users
import com.xzb.model.Kweet
import com.xzb.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.Closeable
import java.time.LocalDateTime

interface DAOFacade : Closeable {
    fun init()

    /**
     * Creates a new [user] in the database from its object [User] representation.
     */
    fun createUser(user: User)


    fun user(userId: String, hash: String? = null): User?

    fun userByEmail(email: String): User?


    /**
     * Creates a Kweet from a specific [user] name, the kweet [text] content,
     * an optional [replyTo] id of the parent kweet, and a [date] that would default to the current time.
     */
    fun createKweet(user: String, text: String, replyTo: Int? = null, date: LocalDateTime = LocalDateTime.now()): Int

    /**
     * Deletes a kweet from its [id].
     */
    fun deleteKweet(id: Int)

    /**
     * Get the DAO object representation of a kweet based from its [id].
     */
    fun getKweet(id: Int): Kweet?


    /**
     * Returns a list of Kweet ids, with the ones with most replies first.
     */
    fun top(count: Int = 10): List<Int>

    /**
     * Returns a list of Kweet ids, with the recent ones first.
     */
    fun latest(count: Int = 10): List<Int>

}

class DAOFacadeDatabase : DAOFacade {
    private val db = Database.connect(DatabaseFactory.create())


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
            Users.select { Users.id.eq(userId) }
                .mapNotNull {
                    if (hash == null || it[Users.passwordHash] == hash) {
                        User(userId, it[Users.email], it[Users.displayName], it[Users.passwordHash])
                    } else {
                        null
                    }
                }.singleOrNull()
        }
    }

    override fun userByEmail(email: String): User? {
        return transaction(db)
        {
            Users.select {
                Users.email.eq(email)
            }.mapNotNull { User(it[Users.id], email, it[Users.displayName], passwordHash = it[Users.passwordHash]) }
                .singleOrNull()
        }
    }

    override fun createKweet(user: String, text: String, replyTo: Int?, date: LocalDateTime): Int {
        return transaction(db) {
            Kweets.insert {
                it[Kweets.text] = text
                it[Kweets.date] = date
                it[Kweets.replyTo] = replyTo
                it[Kweets.directReplyTo] = replyTo
                it[Kweets.user] = user
            }.resultedValues?.firstOrNull()?.get(Kweets.id) ?: error("No generated key returned")
        }
    }

    override fun deleteKweet(id: Int) {
        transaction(db) {
            Kweets.deleteWhere { Kweets.id.eq(id) }
        }
    }


    override fun getKweet(id: Int): Kweet? {
        return transaction(db)
        {
            Kweets.select {
                Kweets.id.eq(id)
            }.mapNotNull {
                Kweet(
                    id = it[Kweets.id], userId = it[Kweets.user], text = it[Kweets.text], date = it[Kweets.date],
                    replyTo = it[Kweets.replyTo]
                )
            }.singleOrNull()
        }
    }

    override fun top(count: Int): List<Int> {
        return transaction(db) {
            val k2 = Kweets.alias("k2")
            Kweets.join(k2, joinType = JoinType.LEFT, Kweets.id, k2[Kweets.replyTo])
                .slice(Kweets.id, k2[Kweets.id])
                .selectAll()
                .groupBy(Kweets.id)
                .orderBy(k2[Kweets.id].count(), order = SortOrder.DESC)
                .limit(count)
                .map { it[Kweets.id] }
        }

    }

    override fun latest(count: Int): List<Int> {
        return transaction(db) {

            val generateSequence = generateSequence(2L) { it * it }.take(10)
            for (minutes in generateSequence) {
                val dt = LocalDateTime.now().minusMinutes(minutes)

                val all = Kweets.slice(Kweets.id).select {
                    Kweets.date.greater(dt)
                }
                    .orderBy(Kweets.date, SortOrder.DESC)
                    .map { it[Kweets.id] }

                if (all.size >= count) {
                    return@transaction all
                }
            }
            return@transaction Kweets.selectAll().limit(count).map { it[Kweets.id] }

        }


        // generate random sequence
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}