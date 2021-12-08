package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.minn.jda.ktx.Embed
import dev.taboo.taboo.Taboo
import dev.taboo.taboo.commands.Suggest.Suggest.suggestionCount
import dev.taboo.taboo.commands.Suggest.Suggest.userId
import dev.taboo.taboo.util.PropertiesManager.suggestionLog
import dev.taboo.taboo.util.ResponseHelper
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.Instant
import java.util.*

class Suggest: SlashCommand() {

    init {
        name = "suggest"
        aliases = arrayOf("suggestion")
        help = "Suggest something to the developers"
        options = mutableListOf(OptionData(OptionType.STRING, "suggestion", "Your suggestion", true))
        guildOnly = true
    }

    object Suggest: Table("Suggest") {
        val userId = varchar("userId", 20).uniqueIndex()
        val suggestionCount = varchar("suggestionCount", 1000000)
        override val primaryKey = PrimaryKey(userId)
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val jda = event.jda
        val suggestion = event.getOption("suggestion")!!.asString
        val guild = event.guild
        val hook = event.hook
        event.deferReply(true).queue()
        hook.sendMessageEmbeds(initialSuggestEmbed(user, suggestion))
            .mentionRepliedUser(false)
            .addActionRow(
                Button.of(ButtonStyle.SECONDARY, "suggest:${user.id}:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "suggest:${user.id}:no", "No")
            ).queue { waitForButtonClick(user, suggestion, jda, guild!!) }
    }

    override fun execute(event: CommandEvent) {
        val author = event.author
        val jda = event.jda
        val suggestion = event.args
        val guild = event.guild
        val message = event.message
        message.replyEmbeds(initialSuggestEmbed(author, suggestion))
            .mentionRepliedUser(false)
            .setActionRow(
                Button.of(ButtonStyle.SECONDARY, "suggest:${author.id}:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "suggest:${author.id}:no", "No")
            ).queue { waitForButtonClick(author, suggestion, jda, guild) }
    }

    private fun waitForButtonClick(user: User, suggestion: String, jda: JDA, guild: Guild) {
        val taboo = Taboo.INSTANCE ?: throw RuntimeException("Taboo is null")
        taboo.waiter.waitForEvent(
            ButtonClickEvent::class.java,
            { clickEvent ->
                if (clickEvent.user != user) return@waitForEvent false
                if (!equalsAny(clickEvent.componentId, user)) return@waitForEvent false
                !clickEvent.isAcknowledged
            }
        ) { clickEvent ->
            val clickUser = clickEvent.user
            val userId = clickUser.id
            val id = clickEvent.componentId.split(":").toTypedArray()[2]
            val hook = clickEvent.hook
            var userCount: String? = null
            clickEvent.deferEdit().queue()
            when (id) {
                "yes" -> {
                    hook.editOriginalEmbeds(finalSuggestEmbed(user, userCount))
                        .setActionRows(emptyList())
                        .queue()
                    sendSuggestion(user, suggestion, jda, guild)
                    transaction {
                        Suggest.insertIgnore { table ->
                            table[Suggest.userId] = userId
                            table[suggestionCount] = (0).toString()
                        }
                    }
                    transaction {
                        userCount = getSuggestionCountFromUser(userId)
                    }
                    transaction {
                        Suggest.replace { table ->
                            table[Suggest.userId] = userId
                            table[suggestionCount] = (userCount!!.toInt() + 1).toString()
                        }
                    }
                    transaction {
                        userCount = getSuggestionCountFromUser(userId)
                    }
                    hook.editOriginalEmbeds(finalSuggestEmbed(user, userCount)).queue()
                }
                "no" -> {
                    hook.editOriginalEmbeds(noSuggestionEmbed(user))
                        .setActionRows(emptyList())
                        .queue()
                }
            }
        }
    }

    private fun getSuggestionCountFromUser(id: String): String {
        return transaction {
            Suggest.select {
                userId eq id
            }
        }.single()[suggestionCount]
    }

    private fun equalsAny(id: String, user: User): Boolean {
        return id == "suggest:${user.id}:yes" || id == "suggest:${user.id}:no"
    }

    private fun initialSuggestEmbed(user: User, suggestion: String): MessageEmbed {
        return Embed {
            title = "Suggestion"
            description =
                """
                    ${user.asMention}, are you sure you want to suggest the following suggestion?
                    **Suggestion:** $suggestion
                """.trimIndent()
            color = 0x9F90CF
            timestamp = Instant.now()
        }
    }

    private fun finalSuggestEmbed(user: User, count: String?): MessageEmbed {
        return Embed {
            title = "Thank you for your suggestion."
            description = "Your suggestion has been sent to the developers. Thank you for your input!"
            color = 0x9F90CF
            footer {
                name = "You have $count suggestions."
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
    }

    private fun noSuggestionEmbed(user: User): MessageEmbed {
        return ResponseHelper.generateSuccessEmbed(
            user, "Suggestion cancelled",
            "", Color.RED
        )
    }

    private fun sendSuggestion(user: User, suggestion: String, jda: JDA, guild: Guild) {
        val suggestionLog = jda.getTextChannelById(suggestionLog)
        val suggestionEmbed = Embed {
            name = "New Suggestion"
            description =
                """
                    **Suggestion:** $suggestion
                    **Suggested by:** ${user.asTag}
                    **Server:** ${guild.name}
                    **Suggested at:** ${Date.from(Instant.now())}
                """.trimIndent()
            color = 0x9F90CF
            footer {
                name = "Sent by ${user.asTag}"
                iconUrl = user.effectiveAvatarUrl
            }
            timestamp = Instant.now()
        }
        suggestionLog!!.sendMessageEmbeds(suggestionEmbed).queue()
    }

}