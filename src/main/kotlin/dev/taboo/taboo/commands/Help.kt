package dev.taboo.taboo.commands

import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.taboo.taboo.database.DatabaseManager
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class Help: SlashCommand() {

    init {
        name = "help"
        help = "Displays all commands."
        aliases = arrayOf("h", "?", "commands", "cmds")
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val guild = event.guild!!
        val guildId = guild.id
        val hook = event.hook
        event.deferReply(true).queue()
        /*transaction {
            hook.sendMessageEmbeds(helpEmbed(user, DatabaseManager.PrefixManager.getPrefixFromGuild(guildId)))
                .mentionRepliedUser(false)
                .queue()
        }*/
    }

    override fun execute(event: CommandEvent) {
        val author = event.author
        val guild = event.guild
        val guildId = guild.id
        val message = event.message
        /*transaction {
            message.replyEmbeds(helpEmbed(author, DatabaseManager.PrefixManager.getPrefixFromGuild(guildId)))
                .mentionRepliedUser(false)
                .queue()
        }*/
    }

    private fun helpEmbed(user: User, prefix: String): MessageEmbed {
        return EmbedBuilder()
            .setTitle("Help")
            .setColor(0x9F90CF)
            .setDescription("All commands are listed below:")
            .addField(prefix + "help", "Displays this message.", false)
            .addField(prefix + "ping", "Pong!", false)
            .addField(prefix + "info", "Displays information about me.", false)
            .addField(prefix + "stats", "Displays my statistics.", false)
            .addField(prefix + "invite", "Displays my invite link.", false)
            .addField(prefix + "support", "Displays a link to my support server.", false)
            .addField(prefix + "suggest", "Suggest a feature for me.", false)
            .addField(prefix + "report", "Report a bug or a feature.", false)
            .setFooter("Requested by ${user.asTag}", user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()
    }

}