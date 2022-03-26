package xyz.chalky.taboo.database

import org.jetbrains.exposed.sql.Table

object Config : Table("config") {
    val guildId = long("guild_id").uniqueIndex()
    val actionLog = long("action_log_id")
    override val primaryKey = PrimaryKey(guildId)
}

object Play : Table("play") {
    val guildId = long("guild_id").uniqueIndex()
    val userId = long("user_id")
    val identifier = text("identifier")
    override val primaryKey = PrimaryKey(guildId)
}

object Queue : Table("queue") {
    val guildId = long("guild_id").uniqueIndex()
    val position = integer("position")
    val userId = long("user_id")
    val identifier = text("identifier")
    override val primaryKey = PrimaryKey(guildId)
}