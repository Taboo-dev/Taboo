package xyz.chalky.taboo.database

import org.jetbrains.exposed.sql.Table

object Config : Table("config") {
    val guildId = long("guild_id").uniqueIndex()
    val actionLog = long("action_log_id")
    val joinLeaveLog = long("join_leave_log_id")
    override val primaryKey = PrimaryKey(guildId)
}

object AutoLeave : Table("auto-leave") {
    val channelId = long("channel_id").uniqueIndex()
    val leave = bool("leave")
    override val primaryKey = PrimaryKey(channelId)
}
