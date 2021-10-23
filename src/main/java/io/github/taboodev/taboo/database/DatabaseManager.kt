package io.github.taboodev.taboo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.taboodev.taboo.Taboo
import io.github.taboodev.taboo.commands.Prefix
import io.github.taboodev.taboo.util.PropertiesManager.SQLPassword
import io.github.taboodev.taboo.util.PropertiesManager.SQLUser
import io.github.taboodev.taboo.util.PropertiesManager.driverClassName
import io.github.taboodev.taboo.util.PropertiesManager.jdbcUrl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {

    private val config = HikariConfig()
    private var dataSource: HikariDataSource

    object Owner: Table("Owner") {
        val guildId = long("guildId").uniqueIndex()
        val ownerId = long("ownerId")
    }

    init {
        config.jdbcUrl = jdbcUrl
        config.username = SQLUser
        config.password = SQLPassword
        config.driverClassName = driverClassName
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        Taboo.LOG_TABOO.info("Connected to database!")
    }

    fun startDb() {
        transaction {
            SchemaUtils.create(Prefix.Prefix)
        }
    }

}