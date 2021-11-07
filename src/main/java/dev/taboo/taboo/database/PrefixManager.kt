package dev.taboo.taboo.database

import dev.taboo.taboo.commands.Settings
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PrefixManager {

    fun getPrefixFromGuild(id: String): String {
        return transaction {
            Settings.SetPrefix.Prefix.select {
                Settings.SetPrefix.Prefix.guildId eq id
            }
        }.single()[Settings.SetPrefix.Prefix.prefix]
    }
}