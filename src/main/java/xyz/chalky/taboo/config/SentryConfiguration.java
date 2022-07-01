package xyz.chalky.taboo.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import io.sentry.Sentry;
import io.sentry.logback.SentryAppender;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentryConfiguration.class);
    private static final String SENTRY_APPENDER_NAME = "SENTRY";

    public SentryConfiguration(@NotNull SentryConfigProperties sentry) {
        String dsn = sentry.getDsn();
        String env = sentry.getEnvironment();
        if (dsn == null || dsn.isEmpty()) {
            turnOffSentry(env);
        } else {
            turnOnSentry(dsn, env);
        }
    }

    private void turnOnSentry(String dsn, String env) {
        LOGGER.info("Sentry is enabled");
        Sentry.init(options -> {
            options.setDsn(dsn);
            if (!env.isBlank()) {
                options.setEnvironment(env);
            }
        });
        getSentryLogbackAppender().start();
    }

    private void turnOffSentry(@NotNull String env) {
        if (env.equalsIgnoreCase("production")) {
            LOGGER.error("Sentry is disabled - Stopping application!");
            System.exit(1);
            return;
        }
        LOGGER.warn("Sentry is disabled");
        Sentry.close();
        getSentryLogbackAppender().stop();
    }

    private synchronized @NotNull SentryAppender getSentryLogbackAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        SentryAppender sentryAppender = (SentryAppender) root.getAppender(SENTRY_APPENDER_NAME);
        if (sentryAppender == null) {
            sentryAppender = new SentryAppender();
            sentryAppender.setName(SENTRY_APPENDER_NAME);

            ThresholdFilter warningsOrAboveFilter = new ThresholdFilter();
            warningsOrAboveFilter.setLevel(Level.WARN.levelStr);
            warningsOrAboveFilter.start();
            sentryAppender.addFilter(warningsOrAboveFilter);

            sentryAppender.setContext(loggerContext);
            root.addAppender(sentryAppender);
        }
        return sentryAppender;
    }

}
