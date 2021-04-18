package com.xzb.datasource

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import javax.sql.DataSource

object DatabaseFactory {

    fun create(): DataSource {
        val hConfig = HoconApplicationConfig(ConfigFactory.load())
        val config = HikariConfig().apply {
            driverClassName = hConfig.property("ktor.datasource.driverClassName").getString()
            username        = hConfig.property("ktor.datasource.username").getString()
            password        = hConfig.property("ktor.datasource.password").getString()
            maximumPoolSize = hConfig.property("ktor.datasource.maximumPoolSize").getString().toInt()
            jdbcUrl = hConfig.property("ktor.datasource.jdbcUrl").getString()
            transactionIsolation = hConfig.property("ktor.datasource.transactionIsolation").getString()

        }

       return HikariDataSource(config)
    }

}