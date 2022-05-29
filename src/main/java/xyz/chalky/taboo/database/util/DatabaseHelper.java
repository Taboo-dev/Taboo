package xyz.chalky.taboo.database.util;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.database.model.Config;
import xyz.chalky.taboo.database.repository.ConfigRepository;

@Component
public record DatabaseHelper(ConfigRepository configRepository) {

    private static DatabaseHelper instance;

    @Autowired
    public DatabaseHelper(ConfigRepository configRepository) {
        this.configRepository = configRepository;
        instance = this;
    }

    public static DatabaseHelper getInstance() {
        return instance;
    }

    public @Nullable Long getLogChannelById(long guildId) {
        return configRepository.findById(guildId).map(Config::getLogChannelId).orElse(null);
    }

    public @Nullable Long getMusicChannelById(long guildId) {
        return configRepository.findById(guildId).map(Config::getMusicChannelId).orElse(null);
    }

}
