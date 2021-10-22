package io.github.taboodev.taboo.util

import java.util.*

object PropertiesManager {

    private var properties: Properties? = null

    fun loadProperties(config: Properties?) {
        properties = config
    }

    @JvmStatic
    val token: String
        get() = properties!!.getProperty("token")
    @JvmStatic
    val ownerId: String
        get() = properties!!.getProperty("owner")
    @JvmStatic
    val botId: String
        get() = properties!!.getProperty("bot")
    @JvmStatic
    val guildId: String
        get() = properties!!.getProperty("guild")
    @JvmStatic
    val actionLog: String
        get() = properties!!.getProperty("actionLog")
    @JvmStatic
    val jdbcUrl: String
        get() = properties!!.getProperty("jdbc")
    @JvmStatic
    val SQLUser: String
        get() = properties!!.getProperty("sqluser")
    @JvmStatic
    val SQLPassword: String
        get() = properties!!.getProperty("sqlpass")

}