package dev.taboo.taboo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.taboo.taboo.Taboo
import dev.taboo.taboo.commands.Settings
import dev.taboo.taboo.util.PropertiesManager.SQLPassword
import dev.taboo.taboo.util.PropertiesManager.SQLUser
import dev.taboo.taboo.util.PropertiesManager.driverClassName
import dev.taboo.taboo.util.PropertiesManager.jdbcUrl
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {

    private val config = HikariConfig()
    private var dataSource: HikariDataSource

    init {
        config.jdbcUrl = jdbcUrl
        config.username = SQLUser
        config.password = SQLPassword
        config.driverClassName = driverClassName
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        Taboo.TABOO_LOG.info("Connected to database!")
    }

    fun startDb() {
        transaction {
            SchemaUtils.create(Settings.SetPrefix.Prefix, Settings.SetChannel.Channel)
        }
    }

}