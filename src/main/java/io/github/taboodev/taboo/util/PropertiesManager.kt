package io.github.taboodev.taboo.util;

import java.util.Properties;

public class PropertiesManager {

    public static Properties properties;

    public static void loadProperties(Properties config) {
        properties = config;
    }

    public static String getToken() {
        return properties.getProperty("token");
    }

    public static String getOwnerId() {
        return properties.getProperty("owner");
    }

    public static String getBotId() {
        return properties.getProperty("bot");
    }

    public static String getGuildId() {
        return properties.getProperty("guild");
    }

    public static String getActionLog() {
        return properties.getProperty("actionLog");
    }

    public static String getJdbcUrl() {
        return properties.getProperty("jdbc");
    }

    public static String getSQLUser() {
        return properties.getProperty("sqluser");
    }

    public static String getSQLPassword() {
        return properties.getProperty("sqlpass");
    }

}
