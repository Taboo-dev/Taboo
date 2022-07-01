package xyz.chalky.taboo.events;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.database.model.Config;
import xyz.chalky.taboo.database.model.GuildSettings;
import xyz.chalky.taboo.database.repository.ConfigRepository;
import xyz.chalky.taboo.database.repository.GuildSettingsRepository;
import xyz.chalky.taboo.util.ResponseHelper;

import java.time.Instant;

import static xyz.chalky.taboo.util.Constants.*;

@Component
public class GuildJoinLeaveListener extends ListenerAdapter {

    private final ConfigRepository configRepository;
    private final GuildSettingsRepository guildSettingsRepository;

    public GuildJoinLeaveListener(ConfigRepository configRepository, GuildSettingsRepository guildSettingsRepository) {
        this.configRepository = configRepository;
        this.guildSettingsRepository = guildSettingsRepository;
    }

    private static final String NOT_SET = String.format("%s Not Set", CROSS_EMOJI);
    private static final String SET = String.format("%s Set", TICK_EMOJI);

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        BaseGuildMessageChannel defaultChannel = guild.getDefaultChannel();
        String description = String.format("""
                Thanks for inviting me to your server! %s
                I can play music, moderate your server, and more!
                To get started, check out the message below!.
                """, BLUSH_EMOJI);
        MessageEmbed welcomeEmbed = new EmbedBuilder()
                .setTitle("Hi! I'm Taboo!")
                .setDescription(EmojiParser.parseToUnicode(description))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        String botInvUrl = "https://discord.com/api/oauth2/authorize?client_id=963732351937044480&permissions=8&scope=bot%20applications.commands";
        String supportInvUrl = "https://discord.gg/WnaDwug5tc";
        defaultChannel.sendMessageEmbeds(welcomeEmbed).setActionRow(
                Button.link(supportInvUrl, "Support Server"),
                Button.link(botInvUrl, "Invite Me")
        ).queue();
        EmbedBuilder configEmbed = new EmbedBuilder()
                .setTitle("Configure Your Server")
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now());
        defaultChannel.sendMessageEmbeds(configEmbed.build()).queue(configMessage -> {
            configEmbed.addField("Log Channel", EmojiParser.parseToUnicode(NOT_SET), false);
            configEmbed.addField("Music Channel", EmojiParser.parseToUnicode(NOT_SET), false);
            configEmbed.setDescription("If you want to change the channels, use the `/config` command, or click the button below!");
            configMessage.editMessageEmbeds(configEmbed.build()).setActionRow(
                    Button.of(ButtonStyle.PRIMARY, String.format("%s:configure", event.getGuild().getId()), "Configure")
            ).queue();
            guildSettingsRepository.save(new GuildSettings(guild.getIdLong(), false, false, configMessage.getIdLong()));
        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.isFromGuild()) return;
        String id = event.getGuild().getId();
        if (event.getComponentId().equals(String.format("%s:configure", id))) {
            TextInput log = TextInput.create(String.format("%s:configure:modal:log", id), "Log Channel", TextInputStyle.SHORT)
                    .setPlaceholder("Enter your Log Channel ID")
                    .setRequired(false)
                    .setRequiredRange(17, 20)
                    .build();
            TextInput music = TextInput.create(String.format("%s:configure:modal:music", id), "Music Channel", TextInputStyle.SHORT)
                    .setPlaceholder("Enter your Music Channel ID")
                    .setRequired(false)
                    .setRequiredRange(17, 20)
                    .build();
            Modal modal = Modal.create(String.format("%s:configure:modal", id), "Configure Your Server")
                    .addActionRows(ActionRow.of(log), ActionRow.of(music))
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Guild guild = event.getGuild();
        String id = guild.getId();
        boolean logFilled, musicFilled;
        String logInput = event.getValue(String.format("%s:configure:modal:log", id)).getAsString();
        logFilled = !logInput.isEmpty();
        String musicInput = event.getValue(String.format("%s:configure:modal:music", id)).getAsString();
        musicFilled = !musicInput.isEmpty();
        long log = Long.parseLong(logInput);
        long music = Long.parseLong(musicInput);
        TextChannel logChannel = guild.getTextChannelById(log);
        TextChannel musicChannel = guild.getTextChannelById(music);
        boolean logCheck = checkChannel(guild, log);
        boolean musicCheck = checkChannel(guild, music);
        if (!logCheck || !musicCheck) {
            String content = String.format("%s One or more of the channels are invalid!", CROSS_EMOJI);
            event.reply(EmojiParser.parseToUnicode(content)).queue();
            return;
        }
        Long messageId = guildSettingsRepository.findById(guild.getIdLong()).map(GuildSettings::getMessageId).orElse(null);
        if (messageId != null) {
            guild.getDefaultChannel().retrieveMessageById(messageId).queue(message -> {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Configure Your Server")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .addField("Log Channel", EmojiParser.parseToUnicode(String.format("%s %s", SET, logChannel.getAsMention())), false)
                        .addField("Music Channel", EmojiParser.parseToUnicode(String.format("%s %s", SET, musicChannel.getAsMention())), false)
                        .setDescription("If you want to change the channels, use the `/config` command, or click the button below!")
                        .build();
                message.editMessageEmbeds(embed).queue();
            });
        }
        guildSettingsRepository.save(new GuildSettings(guild.getIdLong(), logFilled, musicFilled, messageId));
        configRepository.save(new Config(guild.getIdLong(), log, music));
        String content = String.format("%s Your server has been configured!", TICK_EMOJI);
        event.replyEmbeds(
                ResponseHelper.createEmbed(null, EmojiParser.parseToUnicode(content), "0x9F90CF", null).build()
        ).queue();
    }


    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        long id = event.getGuild().getIdLong();
        guildSettingsRepository.findById(id).ifPresent(guildSettings -> {
            guildSettingsRepository.deleteById(id);
        });
        configRepository.findById(id).ifPresent(config -> {
            configRepository.deleteById(id);
        });
    }

    private boolean checkChannel(@NotNull Guild guild, Long channelId) {
        return guild.getTextChannelById(channelId) != null;
    }

}
