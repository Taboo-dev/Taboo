package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.taboo.taboo.util.PropertiesManager
import dev.taboo.taboo.util.ResponseHelper
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class Settings: SlashCommand() {

    init {
        name = "settings"
        aliases = arrayOf("set")
        help = "Change settings for Taboo."
        guildOnly = true
        children = arrayOf(SetChannel(), SetPrefix(), SetExtensions())
    }

    override fun execute(event: SlashCommandEvent) {
        // Ignored because all commands are subcommands.
    }

    override fun execute(event: CommandEvent) {
        TODO("Add embed listing commands")
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
            val member = event.member
            val channel = event.getOption("channel")!!.asGuildChannel
            val channelId = channel.id
            val isAudio = channel.type.isAudio
            val hook = event.hook
            val manager = guild.getRolesByName("Taboo Manager", true)
            val isManager = member!!.roles.stream().anyMatch { manager.contains(it) }
            event.deferReply(true).queue()
            if (!isManager) {
                hook.sendMessageEmbeds(noRoleEmbed(user)).mentionRepliedUser(false).queue()
                return
            }
            if (channel == null) {
                hook.sendMessageEmbeds(noChannelEmbed(user)).mentionRepliedUser(false).queue()
                return
            }
            if (isAudio) {
                hook.sendMessageEmbeds(noChannelEmbed(user)).mentionRepliedUser(false).queue()
                return
            }
            transaction {
                Channel.replace {
                    it[Channel.guildId] = guildId
                    it[Channel.channelId] = channelId
                }
                hook.sendMessageEmbeds(successEmbed(user, channel.asMention)).mentionRepliedUser(false).queue()
            }
        }

        override fun execute(event: CommandEvent) {
            val guild = event.guild
            val guildId = guild!!.id
            val author = event.author
            val member = event.member
            val message = event.message
            val args = event.args
            val manager = guild.getRolesByName("Taboo Manager", true)
            val isManager = member!!.roles.stream().anyMatch { manager.contains(it) }
            val channelId: String = if (args.contains("<#") && args.contains(">")) {
                args.substring(args.indexOf("<#") + 2, args.indexOf(">"))
            } else args
            if (!isManager) {
                message.replyEmbeds(noRoleEmbed(author)).queue()
                return
            }
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

        private fun noRoleEmbed(user: User): MessageEmbed {
            return ResponseHelper.generateFailureEmbed(
                user, "You do not have the required role to use this command!",
                ""
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
            val guildId = guild!!.id
            val newPrefix = event.getOption("prefix")!!.asString
            val selfMember = guild.getMemberById(PropertiesManager.botId)
            val manager = guild.getRolesByName("Taboo Manager", true)
            val isManager = member!!.roles.stream().anyMatch { manager.contains(it) }
            val hook = event.hook
            val jda = event.jda
            var actionLogId: String? = null
            event.deferReply(true).queue()
            if (!isManager) {
                hook.sendMessageEmbeds(noRoleEmbed(user)).mentionRepliedUser(false).queue()
                return
            }
            transaction {
                SetChannel.Channel.insertIgnore {
                    it[SetChannel.Channel.guildId] = guildId
                    it[SetChannel.Channel.channelId] = "0"
                }
            }
            transaction {
                actionLogId = SetChannel.Channel.select {
                    SetChannel.Channel.guildId eq guildId
                }.single()[SetChannel.Channel.channelId]
            }
            if (actionLogId.isNullOrEmpty() || actionLogId.equals("0")) {
                hook.sendMessageEmbeds(ResponseHelper.generateFailureEmbed(
                    user, "No channel set.", """
                    You need to set a channel for me to post logs in.
                    To set a channel via a Slash Command, select the channel.
                    To set a channel via a normal command, ping the channel or give it's id.
                """.trimIndent()
                )).queue()
                return
            }
            val actionLog = actionLogId?.let { jda.getTextChannelById(it) }
            if (actionLog == null) {
                hook.sendMessageEmbeds(ResponseHelper.generateFailureEmbed(
                    user, "Channel does not exist.", """
                    The channel you have set does not exist.
                    **Channel ID:** $actionLogId
                    If you think this is an error, check if the channel exists or if I have access to it.
                """.trimIndent()
                )).queue()
                return
            }
            transaction {
                Prefix.replace {
                    it[Prefix.guildId] = guildId
                    it[prefix] = newPrefix
                }
            }
            hook.sendMessageEmbeds(prefixEmbed(user, newPrefix)).mentionRepliedUser(false).queue {
                selfMember!!.modifyNickname("[$newPrefix] Taboo").queue()
                actionLog.sendMessageEmbeds(prefixEmbed(user, newPrefix)).queue()
            }
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

    class SetExtensions: SlashCommand() {

        init {
            name = "extensions"
            aliases = arrayOf("ext", "setextensions", "fileextensions", "fileext", "setfileextensions")
            help = "Sets the file extensions that I should upload to Hastebin."
            options = mutableListOf(OptionData(OptionType.STRING, "extensions", "The new extensions.").setRequired(true))
            guildOnly = true
        }

        object Extensions: Table("Extensions") {
            val guildId = text("guildId").uniqueIndex()
            val extensions = text("extensions")
            override val primaryKey = PrimaryKey(guildId)
        }

        override fun execute(event: SlashCommandEvent) {
            val user = event.user
            val guild = event.guild
            val id = guild!!.id
            val newExtensions = event.getOption("extensions")!!.asString
            val pattern = Regex("[^a-zA-Z0-9\\-\\.]")
            val hook = event.hook
            event.deferReply(true).queue()
        }

    }

}