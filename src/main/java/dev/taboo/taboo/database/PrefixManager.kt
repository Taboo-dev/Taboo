package dev.taboo.taboo.database

import dev.taboo.taboo.commands.Prefix
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PrefixManager {

    fun getPrefixFromGuild(id: String): String {
        return transaction {
            Prefix.Prefix.select {
                Prefix.Prefix.guildId eq id
            }
        }.single()[Prefix.Prefix.prefix]
    }
}