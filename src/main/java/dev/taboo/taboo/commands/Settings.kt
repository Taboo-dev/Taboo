package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.taboo.taboo.util.PropertiesManager
import dev.taboo.taboo.util.ResponseHelper
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class Settings: SlashCommand() {

    init {
        name = "settings"
        aliases = arrayOf("set")
        help = "Change settings for Taboo."
        guildOnly = false
        // this.children = new SlashCommand[]{ new SetPrefix(), new SetLanguage(), new SetChannel(), new SetRole() };
        children = arrayOf(SetChannel(), SetPrefix())
    }

    override fun execute(event: SlashCommandEvent) {
        // Ignored because all commands are subcommands.
    }

    class SetChannel: SlashCommand() {

        init {
            name = "channel"
            aliases = arrayOf("ch")
            help = "Change the channel Taboo will post logs in."
            options = mutableListOf(OptionData(OptionType.CHANNEL, "channel",
                "The id of the channel that I should log messages in.").setRequired(true))
            guildOnly = true
        }

        object Channel: Table("Channel") {
            val guildId = text("guildId").uniqueIndex()
            val channelId = text("channelId")
            override val primaryKey = PrimaryKey(guildId)
        }

        override fun execute(event: SlashCommandEvent) {
            val guild = event.guild
            val guildId = guild!!.id
            val user = event.user
            val channel = event.getOption("channel")!!.asGuildChannel
            val channelId = channel.id
            val isAudio = channel.type.isAudio
            if (channel == null) {
                event.replyEmbeds(noChannelEmbed(user)).mentionRepliedUser(false).setEphemeral(true).queue()
                return
            }
            if (isAudio) {
                event.replyEmbeds(noChannelEmbed(user)).mentionRepliedUser(false).setEphemeral(true).queue()
                return
            }
            transaction {
                Channel.replace {
                    it[Channel.guildId] = guildId
                    it[Channel.channelId] = channelId
                }
                event.replyEmbeds(successEmbed(user, channel.asMention)).mentionRepliedUser(false).setEphemeral(false).queue()
            }
        }

        override fun execute(event: CommandEvent) {
            val guild = event.guild
            val guildId = guild!!.id
            val author = event.author
            val message = event.message
            val args = event.args
            val channelId: String = if (args.contains("<#") && args.contains(">")) {
                args.substring(args.indexOf("<#") + 2, args.indexOf(">"))
            } else args
            if (args == null || channelId.isEmpty() || channelId.isBlank()) {
                message.replyEmbeds(noChannelEmbed(author)).mentionRepliedUser(false).queue()
                return
            } else {
                val channel = guild.getTextChannelById(channelId)
                if (channel == null) {
                    message.replyEmbeds(noChannelEmbed(author)).mentionRepliedUser(false).queue()
                    return
                }
                val isAudio = channel.type.isAudio
                if (isAudio) {
                    message.replyEmbeds(noChannelEmbed(author)).mentionRepliedUser(false).queue()
                    return
                }
                transaction {
                    Channel.replace {
                        it[Channel.guildId] = guildId
                        it[Channel.channelId] = channelId
                    }
                    message.replyEmbeds(successEmbed(author, channel.asMention)).mentionRepliedUser(false).queue()
                }
            }
        }

        private fun noChannelEmbed(user: User): MessageEmbed {
            return ResponseHelper.generateFailureEmbed(
                user,
                "You have not set a channel for me to post logs in, or it is an audio channel.",
                """
                If you think you have set a channel, it may be that this channel is non-existent or that I cannot access it, or that this channel is a VC.
                To set a channel via a Slash Command, select the channel.
                To set a channel via a normal command, ping the channel or give it's id.
                """.trimIndent()
            )
        }

        private fun successEmbed(user: User, channelMention: String): MessageEmbed {
            return ResponseHelper.generateSuccessEmbed(
                user,
                "Channel set successfully.",
                "I will now post logs in $channelMention.",
                Color.decode("0x9F90CF")
            )
        }

    }

    class SetPrefix: SlashCommand() {

        init {
            name = "prefix"
            aliases = arrayOf("setprefix", "setcmdprefix", "setcommandprefix", "pre", "p")
            help = "Sets the bot's prefix."
            options = mutableListOf(OptionData(OptionType.STRING, "prefix", "The new prefix.").setRequired(true))
            guildOnly = true
        }

        object Prefix: Table("Prefix") {
            val guildId = text("guildId").uniqueIndex()
            val prefix = text("prefix")
            override val primaryKey = PrimaryKey(guildId)
        }

        override fun execute(event: SlashCommandEvent) {
            val user = event.user
            val member = event.member
            val guild = event.guild
            val id = guild!!.id
            val newPrefix = event.getOption("prefix")!!.asString
            val selfMember = guild.getMemberById(PropertiesManager.botId)
            val manager = guild.getRolesByName("Taboo Manager", true)
            val isManager = member!!.roles.stream().anyMatch { manager.contains(it) }
            if (isManager) {
                transaction {
                    Prefix.replace {
                        it[guildId] = id
                        it[prefix] = newPrefix
                    }
                }
                event.replyEmbeds(prefixEmbed(user, newPrefix)).mentionRepliedUser(false).setEphemeral(false).queue {
                    selfMember!!.modifyNickname("[$newPrefix] Taboo").queue()
                }
            } else event.replyEmbeds(noRoleEmbed(user)).mentionRepliedUser(false).setEphemeral(true).queue()
        }

        override fun execute(event: CommandEvent) {
            val user = event.author
            val member = event.member
            val guild = event.guild
            val id = guild!!.id
            val newPrefix = event.args
            val message = event.message
            val selfMember = guild.getMemberById(PropertiesManager.botId)
            val manager = guild.getRolesByName("Taboo Manager", true)
            val isManager = member!!.roles.stream().anyMatch { manager.contains(it) }
            if (isManager) {
                if (newPrefix.isEmpty()) {
                    message.replyEmbeds(noArgsEmbed(user)).mentionRepliedUser(false).queue()
                    return
                }
                transaction {
                    Prefix.replace {
                        it[guildId] = id
                        it[prefix] = newPrefix
                    }
                }
                message.replyEmbeds(prefixEmbed(user, newPrefix)).mentionRepliedUser(false).queue {
                    selfMember!!.modifyNickname("[$newPrefix] Taboo").queue()
                }
            } else message.replyEmbeds(noRoleEmbed(user)).mentionRepliedUser(false).queue()
        }

        private fun prefixEmbed(user: User, newPrefix: String): MessageEmbed {
            return ResponseHelper.generateSuccessEmbed(
                user, "Prefix changed to `$newPrefix`",
                "", Color.GREEN
            )
        }

        private fun noArgsEmbed(user: User): MessageEmbed {
            return ResponseHelper.generateFailureEmbed(
                user, "No arguments were provided!",
                ""
            )
        }

        private fun noRoleEmbed(user: User): MessageEmbed {
            return ResponseHelper.generateFailureEmbed(
                user, "You do not have the required role to use this command!",
                ""
            )
        }

    }

}