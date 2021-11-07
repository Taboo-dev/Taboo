package dev.taboo.taboo.util

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
    val ownerId: Long
        get() = properties!!.getProperty("owner").toLong()
    @JvmStatic
    val botId: Long
        get() = properties!!.getProperty("bot").toLong()
    @JvmStatic
    val guildId: Long
        get() = properties!!.getProperty("guild").toLong()
    @JvmStatic
    val actionLog: Long
        get() = properties!!.getProperty("actionLog").toLong()
    @JvmStatic
    val jdbcUrl: String
        get() = properties!!.getProperty("jdbc")
    @JvmStatic
    val SQLUser: String
        get() = properties!!.getProperty("sqluser")
    @JvmStatic
    val SQLPassword: String
        get() = properties!!.getProperty("sqlpass")
    @JvmStatic
    val driverClassName: String
        get() = properties!!.getProperty("driverClassName")

}