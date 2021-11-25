package dev.taboo.taboo.interactions

import io.sentry.Sentry
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.Button
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

class Bookmark: ListenerAdapter() {

    override fun onMessageContextCommand(event: MessageContextCommandEvent) {
        val name = event.name
        if (name == "Bookmark") {
            val user = event.user
            val userId = user.id
            val targetMessage = event.targetMessage
            val hook = event.hook
            event.deferReply(true).queue()
            executeResponse(user, userId, hook, targetMessage)
        }
    }

    object Bookmark: Table("Bookmark") {
        val userId = text("userId").uniqueIndex()
        val bookmarkCount = text("bookmarkCount")
        override val primaryKey = PrimaryKey(userId)
    }

    private fun getBookmarkCountFromUser(id: String): String {
        return transaction {
            Bookmark.select {
                Bookmark.userId eq id
            }
        }.single()[Bookmark.bookmarkCount]
    }

    private fun executeResponse(user: User, userId: String, hook: InteractionHook, message: Message) {
        var count: String? = null
        val guild = message.guild
        val jumpUrl = message.jumpUrl
        var contentDisplay = message.contentDisplay
        if (contentDisplay.length > 100) {
            contentDisplay = contentDisplay.substring(0, 99) + "..."
        }
        val author = message.author
        val messageId = message.id
        val timeCreated = message.timeCreated
        val attachments = message.attachments
        if (attachments.isNotEmpty()) {
            for (attachment in attachments) {
                val attachmentUrl = attachment.url
                contentDisplay += "\n$attachmentUrl"
            }
        }
        val bookmarkEmbed = EmbedBuilder()
            .setTitle("Bookmark")
            .setDescription(
                """
                    ***Message Information:***
                    **Content:** $contentDisplay
                    **Server:** ${guild.name}
                    **Author:** ${author.asTag}
                    **Time Created:** ${timeCreated.format(RFC_1123_DATE_TIME)}
                    **Message ID:** $messageId
                """.trimIndent()
            ).setColor(0x9F90CF)
            .setFooter("Bookmarked by ${user.asTag}", user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
        user.openPrivateChannel()
            .flatMap { channel ->
                transaction {
                    Bookmark.insertIgnore { table ->
                        table[Bookmark.userId] = userId
                        table[bookmarkCount] = (0).toString()
                    }
                }
                transaction {
                    count = getBookmarkCountFromUser(userId)
                }
                transaction {
                    Bookmark.replace { table ->
                        table[Bookmark.userId] = userId
                        table[bookmarkCount] = (count!!.toInt() + 1).toString()
                    }
                }
                transaction {
                    count = getBookmarkCountFromUser(userId)
                }
                channel.sendMessageEmbeds(bookmarkEmbed.setTitle("Bookmark $count").build()).setActionRow(
                    Button.link(jumpUrl, "View")
                )
            }.submit()
            .thenAcceptAsync {
                hook.sendMessageEmbeds(bookmarkEmbed.setTitle("Bookmarked Message!").build())
                    .mentionRepliedUser(false).setEphemeral(true).addActionRow(
                        Button.link(jumpUrl, "View")
                    ).queue()
            }.exceptionally { exception ->
                Sentry.captureException(exception)
                hook.sendMessageEmbeds(dmsDisabledEmbed(user)).mentionRepliedUser(false).setEphemeral(true).queue()
                return@exceptionally null
            }
    }

    private fun dmsDisabledEmbed(user: User): MessageEmbed {
        return EmbedBuilder()
            .setTitle("Bookmarking Disabled!")
            .setDescription("You have your DMs disabled. This means that I cannot DM you the bookmark.")
            .setColor(0x9F90CF)
            .setFooter("Requested by ${user.asTag}", user.effectiveAvatarUrl)
            .setTimestamp(Instant.now())
            .build()
    }

}