package dev.taboo.taboo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.taboo.taboo.Taboo
import dev.taboo.taboo.commands.Settings
import dev.taboo.taboo.commands.Suggest
import dev.taboo.taboo.interactions.Bookmark
import dev.taboo.taboo.util.PropertiesManager
import io.sentry.Sentry
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path

object DatabaseManager {

    private val config = HikariConfig()
    private val dataSource: HikariDataSource

    init {
        val database = Path.of("database.db")
        if (Files.notExists(database)) {
            Files.createFile(database)
            Taboo.LOGGER.info("Created database file!")
        }
        config.jdbcUrl = PropertiesManager.jdbcUrl
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        Taboo.LOGGER.info("Connected to database!")
    }

    fun startDb() {
        try {
            transaction {
                SchemaUtils.create(Settings.SetPrefix.Prefix, Settings.SetChannel.Channel, Bookmark.Bookmark, Suggest.Suggest)
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    object PrefixManager {

        fun getPrefixFromGuild(id: String): String {
            return transaction {
                Settings.SetPrefix.Prefix.select {
                    Settings.SetPrefix.Prefix.guildId eq id
                }
            }.single()[Settings.SetPrefix.Prefix.prefix]
        }

    }

}