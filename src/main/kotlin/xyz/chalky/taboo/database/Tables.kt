package xyz.chalky.taboo.database

import org.jetbrains.exposed.sql.Table

object Config : Table("config") {
    val guildId = long("guild_id").uniqueIndex()
    val log = long("log_id")
    override val primaryKey = PrimaryKey(guildId)
}
