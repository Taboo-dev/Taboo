package io.github.taboodev.taboo.util;

import io.github.cdimascio.dotenv.Dotenv;

public interface Constants {
    Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    String TOKEN = dotenv.get("TOKEN");
    String OWNER_ID = dotenv.get("OWNER_ID");
    String BOT_ID = dotenv.get("BOT_ID");
    String GUILD_ID = dotenv.get("GUILD_ID");
    String ACTION_LOG = dotenv.get("ACTION_LOG");
    String JDBC_URL = dotenv.get("JDBC_URL");
    String SQL_USER = dotenv.get("SQL_USER");
    String SQL_PASS = dotenv.get("SQL_PASS");
}
