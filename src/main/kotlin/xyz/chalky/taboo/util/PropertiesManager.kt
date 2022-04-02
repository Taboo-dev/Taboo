package xyz.chalky.taboo.util

import java.net.URI
import java.util.Properties

data class PropertiesManager(val properties: Properties) {

    val token: String
        get() = properties.getProperty("token")

    val debugState: Boolean
        get() = properties.getProperty("debug").toBoolean()

    val guildId: Long
        get() = properties.getProperty("guildId").toLong()

    val ownerId: Long
        get() = properties.getProperty("ownerId").toLong()

    val actionLogId: Long
        get() = properties.getProperty("actionLogId").toLong()

    val lavalinkHost: URI
        get() = URI(properties.getProperty("lavalinkHost"))

    val lavalinkPassword: String
        get() = properties.getProperty("lavalinkPassword")

    val youTubeApiKey: String
        get() = properties.getProperty("youtubeApiKey")

}
