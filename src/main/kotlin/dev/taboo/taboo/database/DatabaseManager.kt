package dev.taboo.taboo.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.taboo.taboo.Taboo
import dev.taboo.taboo.util.PropertiesManager
import io.sentry.Sentry
import okhttp3.internal.http2.Settings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseManager {

    private val config = HikariConfig()
    private val dataSource: HikariDataSource

    init {
        config.jdbcUrl = PropertiesManager.jdbcUrl
        config.username = PropertiesManager.SQLUser
        config.password = PropertiesManager.SQLPassword
        config.driverClassName = PropertiesManager.driverClassName
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
            return transaction<String> {
                Settings.SetPrefix.Prefix.select {
                    Setting.SetPrefix.Prefix.guildId eq id
                }
            }.single()[Settings.SetPrefix.Prefix.prefix]
        }

    }

}