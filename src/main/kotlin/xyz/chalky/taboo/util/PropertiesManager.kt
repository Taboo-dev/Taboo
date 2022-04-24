package xyz.chalky.taboo.util

import java.net.URI
import java.util.*

data class PropertiesManager(val properties: Properties) {

    val token: String
        get() = properties.getProperty("token")

    val debugState: Boolean
        get() = properties.getProperty("debug").toBoolean()

    val guildId: Long
        get() = properties.getProperty("guildId").toLong()

    val ownerId: Long
        get() = properties.getProperty("ownerId").toLong()

    val logId: Long
        get() = properties.getProperty("logId").toLong()

    val lavalinkHost: URI
        get() = URI(properties.getProperty("lavalinkHost"))

    val lavalinkPassword: String
        get() = properties.getProperty("lavalinkPassword")

    val spotifyClientId: String
        get() = properties.getProperty("spotifyClientId")

    val spotifyClientSecret: String
        get() = properties.getProperty("spotifyClientSecret")

}
