package xyz.chalky.taboo.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "guild_settings")
public class GuildSettings {

    @Id
    Long guildId;
    @Column(name = "log_id", nullable = true)
    Boolean hasLogId;
    @Column(name = "music_id", nullable = true)
    Boolean hasMusicId;
    @Column(name = "message_id", nullable = false)
    Long messageId;

    public GuildSettings() {}

    public GuildSettings(Long guildId, Boolean hasLogId, Boolean hasMusicId, Long messageId) {
        this.guildId = guildId;
        this.hasLogId = hasLogId;
        this.hasMusicId = hasMusicId;
        this.messageId = messageId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Boolean getHasLogId() {
        return hasLogId;
    }

    public void setHasLogId(Boolean hasLogId) {
        this.hasLogId = hasLogId;
    }

    public Boolean getHasMusicId() {
        return hasMusicId;
    }

    public void setHasMusicId(Boolean hasMusicId) {
        this.hasMusicId = hasMusicId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

}
