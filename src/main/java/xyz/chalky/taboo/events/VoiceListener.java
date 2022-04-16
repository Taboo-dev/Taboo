package xyz.chalky.taboo.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.chalky.taboo.Taboo;
import xyz.chalky.taboo.music.AudioScheduler;
import xyz.chalky.taboo.music.GuildAudioPlayer;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VoiceListener extends ListenerAdapter {

    private final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        if (!guild.getSelfMember().getVoiceState().isGuildDeafened()) {
            guild.deafen(guild.getSelfMember(), true).queue();
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        AudioChannel voiceChannel = event.getChannelLeft();
        List<Member> members = voiceChannel.getMembers();
        GuildAudioPlayer guildAudioPlayer = Taboo.getInstance().getAudioManager().getAudioPlayer(guild.getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Voice Channel Left")
                .setDescription(String.format("I have left the voice channel %s because I was alone.", voiceChannel.getAsMention()))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        if ((members.size() == 1) && (members.get(0).getIdLong() == jda.getSelfUser().getIdLong())) {
            ses.schedule(() -> {
                if (voiceChannel.getMembers().size() > 1) { // Check if there are still people in the voice channel
                    return; // Abort the SES
                }
                long channelId = scheduler.getChannelId();
                TextChannel channel = guild.getTextChannelById(channelId);
                scheduler.destroy();
                jda.getDirectAudioController().disconnect(guild);
                channel.sendMessageEmbeds(embed).queue();
            }, 2, TimeUnit.MINUTES);
        }
    }

}
