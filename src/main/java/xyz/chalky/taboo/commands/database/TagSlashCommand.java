package xyz.chalky.taboo.commands.database;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import xyz.chalky.taboo.core.SlashCommand;
import xyz.chalky.taboo.database.model.Tag;
import xyz.chalky.taboo.database.repository.TagRepository;
import xyz.chalky.taboo.util.ResponseHelper;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TagSlashCommand extends SlashCommand {

    private final TagRepository tagRepository;

    public TagSlashCommand(TagRepository tagRepository) {
        setCommandData(
                Commands.slash("tag", "Tag commands")
                        .addSubcommands(
                                new SubcommandData("create", "Creates a tag.").addOptions(
                                        new OptionData(OptionType.STRING, "name", "The name of the tag.", true, false),
                                        new OptionData(OptionType.STRING, "title", "The title of the tag.", true, false),
                                        new OptionData(OptionType.STRING, "value", "The content of the tag.", true, false)
                                ), new SubcommandData("delete", "Deletes a tag.").addOptions(
                                        new OptionData(OptionType.STRING, "name", "The name of the tag.", true, true)
                                ), new SubcommandData("list", "Lists all tags."),
                                new SubcommandData("get", "Gets a tag.").addOptions(
                                        new OptionData(OptionType.STRING, "name", "The name of the tag.", true, true)
                                )
                        )
        );
        setEphemeral(false);
        this.tagRepository = tagRepository;
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        long guildId = event.getGuild().getIdLong();
        switch (subcommandName) {
            case "create" -> {
                String tagName = event.getOption("name").getAsString();
                String tagTitle = event.getOption("title").getAsString();
                String tagValue = event.getOption("value").getAsString();
                tagRepository.findByGuildId(guildId)
                        .stream()
                        .filter(tag -> tag.getName().equals(tagName))
                        .findFirst()
                        .ifPresentOrElse(tag -> {
                            MessageEmbed tagAlreadyPresent = ResponseHelper.createEmbed(null, "Tag already present", Color.RED, event.getUser()).build();
                            event.getHook().sendMessageEmbeds(tagAlreadyPresent).queue();
                        }, () -> {
                            tagRepository.save(new Tag(guildId, tagName, tagTitle, tagValue));
                            MessageEmbed tagSaved = ResponseHelper.createEmbed(null, "Tag saved", Color.GREEN, event.getUser()).build();
                            event.getHook().sendMessageEmbeds(tagSaved).queue();
                        });
            }
            case "delete" -> {
                String tagName = event.getOption("name").getAsString();
                tagRepository.findByGuildId(guildId)
                        .stream()
                        .filter(tag -> tag.getName().equals(tagName))
                        .findFirst()
                        .ifPresentOrElse(tag -> {
                            tagRepository.delete(tag);
                            MessageEmbed tagDeleted = ResponseHelper.createEmbed(null, "Tag deleted", Color.GREEN, event.getUser()).build();
                            event.getHook().sendMessageEmbeds(tagDeleted).queue();
                        }, () -> {
                            MessageEmbed tagNotFound = ResponseHelper.createEmbed(null, "Tag not found", Color.RED, event.getUser()).build();
                            event.getHook().sendMessageEmbeds(tagNotFound).queue();
                        });
            }
            case "list" -> {
                List<Tag> tags = tagRepository.findByGuildId(guildId)
                        .stream()
                        .toList();
                EmbedBuilder tagsList = ResponseHelper.createEmbed(null, "Tags", Color.GREEN, event.getUser());
                tags.forEach(tag -> tagsList.addField(tag.getName(), tag.getTitle(), false));
                event.getHook().sendMessageEmbeds(tagsList.build()).queue();
            }
            case "get" -> {
                String tagName = event.getOption("name").getAsString();
                tagRepository.findByGuildId(guildId)
                        .stream()
                        .filter(tag -> tag.getName().equals(tagName))
                        .findFirst()
                        .ifPresentOrElse(tag -> {
                            MessageEmbed tagFound = ResponseHelper.createEmbed(tag.getTitle(), tag.getValue(), Color.CYAN, event.getUser()).build();
                            event.getHook().sendMessageEmbeds(tagFound).queue();
                        }, () -> {
                            MessageEmbed tagNotFound = ResponseHelper.createEmbed(null, "Tag not found", Color.RED, event.getUser()).build();
                            event.getHook().sendMessageEmbeds(tagNotFound).queue();
                        });
            }
        }
    }

    @Override
    public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        long guildId = event.getGuild().getIdLong();
        String value = focusedOption.getValue();
        if (focusedOption.getName().equals("name")) {
            Set<Command.Choice> choices;
            if (value.isEmpty()) {
                choices = tagRepository.findByGuildId(guildId)
                        .stream()
                        .limit(25)
                        .map(tag -> new Command.Choice(tag.getName(), tag.getName()))
                        .collect(Collectors.toSet());
            } else {
                choices = tagRepository.findByGuildId(guildId)
                        .stream()
                        .limit(25)
                        .filter(tag -> tag.getTitle().contains(value.toLowerCase()))
                        .map(tag -> new Command.Choice(tag.getName(), tag.getName()))
                        .collect(Collectors.toSet());
            }
            event.replyChoices(choices).queue();
        }
    }

}
