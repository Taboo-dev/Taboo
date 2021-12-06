package dev.taboo.taboo.util

import java.util.*

object PropertiesManager {

    private var properties: Properties? = null

    fun loadProperties(file: Properties) {
        properties = file
    }

    @JvmStatic
    val token: String
        get() = properties!!.getProperty("token").toString()
    @JvmStatic
    val ownerId: String
        get() = properties!!.getProperty("owner").toString()
    @JvmStatic
    val botId: String
        get() = properties!!.getProperty("bot").toString()
    @JvmStatic
    val guildId: String
        get() = properties!!.getProperty("guild").toString()
    @JvmStatic
    val actionLog: String
        get() = properties!!.getProperty("actionLog").toString()
    @JvmStatic
    val suggestionLog: String
        get() = properties!!.getProperty("suggestionLog").toString()
    @JvmStatic
    val jdbcUrl: String
        get() = properties!!.getProperty("jdbc")
    @JvmStatic
    val sentryDsn: String
        get() = properties!!.getProperty("sentryDsn")

}