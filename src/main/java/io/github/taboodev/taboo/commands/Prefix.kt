package io.github.taboodev.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.transactions.transaction

class Prefix : SlashCommand() {

    init {
        name = "prefix"
        help = "Sets the bot's prefix."
        options = mutableListOf(OptionData(OptionType.STRING, "prefix", "The new prefix.").setRequired(true))
        guildOnly = true
    }

    object Prefix: Table("Prefix") {
        val guildId = text("guildId").uniqueIndex()
        val prefix = text("prefix")
        override val primaryKey = PrimaryKey(guildId)
    }

    override fun execute(event: CommandEvent) {
        val user = event.author
        val guild = event.guild!!.id
        val newPrefix = event.args
        val message = event.message
        if (newPrefix.isEmpty()) {
            message.reply("There is no prefix to change to!").queue()
            return
        }
        transaction {
            Prefix.replace {
                it[guildId] = guild
                it[prefix] = newPrefix
            }
        }
        val message1 = "${user.asTag} changed the prefix to $newPrefix"
        message.reply(message1).queue()
        TODO("send embeds")
    }

    override fun execute(event: SlashCommandEvent) {

    }

}