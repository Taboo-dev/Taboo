package xyz.chalky.taboo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import xyz.chalky.taboo.util.PropertiesManager

class DatabaseManager(propertiesManager: PropertiesManager) {

    val logger = LoggerFactory.getLogger(DatabaseManager::class.java)!!
    private val config = HikariConfig()
    private var dataSource: HikariDataSource

    init {
        config.jdbcUrl = propertiesManager.jdbcUrl
        config.username = propertiesManager.jdbcUsername
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        logger.info("Connected to database")
    }

    fun startDatabase() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Config)
        }
    }

}