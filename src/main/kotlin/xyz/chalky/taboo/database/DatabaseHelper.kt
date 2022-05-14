package xyz.chalky.taboo.database

import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

data class ConfigEntry(val logChannelId: Long, val musicChannelId: Long)

data class ResponseEntry(val response: String)

fun Guild.insertConfig(logChannelId: Long, musicChannelId: Long) {
    transaction {
        Config.insertIgnore {
            it[guildId] = this@insertConfig.idLong
            it[log] = logChannelId
            it[music] = musicChannelId
        }
    }
}

fun Guild.clearConfig() {
    transaction {
        Config.deleteWhere {
            Config.guildId eq this@clearConfig.idLong
        }
    }
}

fun Guild.getLogChannelId() : Long? {
    var channelId: Long? = null
    transaction {
        channelId = Config.select {
            Config.guildId eq this@getLogChannelId.idLong
        }.firstOrNull()?.getOrNull(Config.log) ?: return@transaction null
    }
    return channelId
}

fun Guild.getMusicChannelId() : Long? {
    var channelId: Long? = null
    transaction {
        channelId = Config.select {
            Config.guildId eq this@getMusicChannelId.idLong
        }.firstOrNull()?.getOrNull(Config.music) ?: return@transaction null
    }
    return channelId
}