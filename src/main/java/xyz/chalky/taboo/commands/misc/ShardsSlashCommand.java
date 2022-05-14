package xyz.chalky.taboo.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.core.SlashCommand;

import java.time.Instant;

public class ShardsSlashCommand extends SlashCommand {

    public ShardsSlashCommand() {
        setCommandData(Commands.slash("shards", "Checks the shard you are on."));
        setEphemeral(true);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        JDA.ShardInfo shardInfo = event.getJDA().getShardInfo();
        String shardId = shardInfo.getShardString();
        long guildId = event.getGuild().getIdLong();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Shard Info:")
                .setDescription(String.format(
                        """
                        ```Server ID: %s```
                        ```Shard ID: %s```
                        """
                , guildId, shardId))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}
