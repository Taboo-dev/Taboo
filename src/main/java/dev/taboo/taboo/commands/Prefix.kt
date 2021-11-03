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

class Prefix: SlashCommand() {

    init {
        name = "prefix"
        help = "Sets the bot's prefix."
        aliases = arrayOf("pre", "p")
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