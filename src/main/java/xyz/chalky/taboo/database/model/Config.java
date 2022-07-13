package xyz.chalky.taboo.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "config")
public class Config {

    @Id
    private Long guildId;
    private Long logChannelId;

    public Config() {}

    public Config(long guildId, long logChannelId) {
        this.guildId = guildId;
        this.logChannelId = logChannelId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public Long getLogChannelId() {
        return logChannelId;
    }

    public void setLogChannelId(long logChannelId) {
        this.logChannelId = logChannelId;
    }

}
