package xyz.chalky.taboo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

object DatabaseManager {

    val logger = KotlinLogging.logger { }
    private val config = HikariConfig()
    private var dataSource: HikariDataSource

    init {
        config.jdbcUrl = "jdbc:sqlite:database.db"
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        logger.info { "Connected to database" }
    }

    fun startDatabase() {
        try {
            val database = Path.of("database.db")
            if (Files.notExists(database)) {
                logger.info { "Database file does not exist! Creating..." }
                Files.createFile(database)
            }
        } catch (e: IOException) {
            logger.error { "Error creating database file" }
            e.printStackTrace()
            exitProcess(1)
        }
        transaction {
            SchemaUtils.createMissingTablesAndColumns(Config)
        }
    }

}