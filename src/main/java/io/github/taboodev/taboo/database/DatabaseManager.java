package io.github.taboodev.taboo.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.taboodev.taboo.Taboo;
import io.github.taboodev.taboo.util.Constants;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private static final HikariConfig config = new HikariConfig();
    private static final HikariDataSource dataSource;

    static {
        config.setJdbcUrl(Constants.JDBC_URL);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setUsername(Constants.SQL_USER);
        config.setPassword(Constants.SQL_PASS);
        dataSource = new HikariDataSource(config);
        try (var statement = getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS owner(guild_id INTEGER UNIQUE, id INTEGER)");
            Taboo.LOG_TABOO.info("Owner table initialised!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to use wherever any SQL requests are made.
     * @return The SQL connection.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
