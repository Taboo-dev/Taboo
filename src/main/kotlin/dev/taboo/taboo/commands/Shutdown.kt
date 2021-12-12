package dev.taboo.taboo.commands

import com.jagrosh.jdautilities.command.CommandEvent
import com.jagrosh.jdautilities.command.SlashCommand
import dev.taboo.taboo.Taboo
import dev.taboo.taboo.util.PropertiesManager
import dev.taboo.taboo.util.PropertiesManager.actionLog
import dev.taboo.taboo.util.ResponseHelper
import io.sentry.Sentry
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import java.awt.Color
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class Shutdown: SlashCommand() {

    init {
        name = "shutdown"
        help = "Shuts down the bot"
        defaultEnabled = false
        enabledUsers = arrayOf(PropertiesManager.ownerId)
        ownerCommand = true
        guildOnly = true
    }

    override fun execute(event: SlashCommandEvent) {
        val user = event.user
        val jda = event.jda
        val hook = event.hook
        val actionLog = jda.getTextChannelById(PropertiesManager.actionLog)
        event.deferReply(true).queue()
        hook.sendMessageEmbeds(initialShutdownEmbed(user))
            .addActionRow(
                Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
            ).mentionRepliedUser(false)
            .queue ({ waitForButtonClick(user, actionLog!!) }, { Sentry.captureException(it) })
    }

    override fun execute(event: CommandEvent) {
        val author = event.author
        val jda = event.jda
        val message = event.message
        val actionLog = jda.getTextChannelById(actionLog)
        message.replyEmbeds(initialShutdownEmbed(author))
            .setActionRow(
                Button.of(ButtonStyle.SECONDARY, "shutdown:yes", "Yes"),
                Button.of(ButtonStyle.SECONDARY, "shutdown:no", "No")
            ).mentionRepliedUser(false)
            .queue( { waitForButtonClick(author, actionLog!!) }, { Sentry.captureException(it) } )
    }

    private fun initialShutdownEmbed(user: User): MessageEmbed {
        return ResponseHelper.generateSuccessEmbed(
            user,
            "Shut down",
            "Do you want to shut me down? Respond with the buttons below.",
            Color.RED
        )
    }

    private fun finalShutdownEmbed(user: User): MessageEmbed {
        return ResponseHelper.generateSuccessEmbed(
            user,
            "Shutting down...",
            "Note: It may take a few minutes for Discord to update my presence and say that I am offline.",
            Color.RED
        )
    }

    private fun noShutdownEmbed(user: User): MessageEmbed {
        return ResponseHelper.generateFailureEmbed(
            user,
            "Shutdown cancelled",
            ""
        )
    }

    private fun waitForButtonClick(user: User, actionLog: TextChannel) {
        val taboo = Taboo.INSTANCE ?: throw RuntimeException("Taboo is null")
        taboo.waiter.waitForEvent(
            ButtonClickEvent::class.java,
            { clickEvent ->
                if (clickEvent.user != user) return@waitForEvent false
                if (!equalsAny(clickEvent.componentId)) return@waitForEvent false
                !clickEvent.isAcknowledged
            }
        ) { clickEvent ->
            val clickUser = clickEvent.user
            val id = clickEvent.componentId.split(":").toTypedArray()[1]
            val hook = clickEvent.hook
            clickEvent.deferEdit().queue()
            when (id) {
                "yes" -> {
                    hook.editOriginalEmbeds(finalShutdownEmbed(clickUser))
                        .setActionRows(emptyList())
                        .submit()
                        .thenAcceptAsync {
                            actionLog.sendMessageEmbeds(finalShutdownEmbed(clickUser)).queue()
                            try {
                                TimeUnit.SECONDS.sleep(10L)
                            } catch (e: InterruptedException) {
                                Sentry.captureException(e)
                            }
                            taboo.jda.setStatus(OnlineStatus.OFFLINE)
                            taboo.jda.shutdown()
                            exitProcess(0)
                        }
                } "no" -> {
                    hook.editOriginalEmbeds(noShutdownEmbed(clickUser))
                        .setActionRows(emptyList())
                        .queue()
                }
            }
        }
    }

    private fun equalsAny(id: String): Boolean {
        return id == "shutdown:yes" || id == "shutdown:no"
    }

}