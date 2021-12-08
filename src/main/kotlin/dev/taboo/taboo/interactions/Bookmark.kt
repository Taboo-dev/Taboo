package dev.taboo.taboo.interactions

import dev.taboo.taboo.util.ResponseHelper
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME

class Bookmark: ListenerAdapter() {

    object Bookmark: Table("Bookmark") {
        val userId = varchar("userId", 20).uniqueIndex()
        val bookmarkCount = varchar("bookmarkCount", 20)
        override val primaryKey = PrimaryKey(userId)
    }

    override fun onMessageContextCommand(event: MessageContextCommandEvent) {
        val name = event.name
        if (name == "Bookmark") {
            val user = event.user
            val userId = user.id
            val targetMessage = event.targetMessage
            val hook = event.hook
            event.deferReply(true).queue()
            execute(user, userId, hook, targetMessage)
        }
    }

    private fun getBookmarkCountFromUser(id: String): String {
        return transaction {
            Bookmark.select {
                Bookmark.userId eq id
            }
        }.single()[Bookmark.bookmarkCount]
    }

    private fun execute(user: User, userId: String, hook: InteractionHook, message: Message) {
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
                channel.sendMessageEmbeds(bookmarkEmbed(contentDisplay, guild, author, timeCreated, messageId, user)
                    .setTitle("Bookmark $count").build())
                    .setActionRow(Button.link(jumpUrl, "View"))
            }.submit()
            .thenAcceptAsync {
                hook.sendMessageEmbeds(bookmarkEmbed(contentDisplay, guild, author, timeCreated, messageId, user)
                    .setTitle("Bookmarked Message!").build())
                    .addActionRow(Button.link(jumpUrl, "View"))
                    .queue()
            }.exceptionally { ex ->
                hook.sendMessageEmbeds(dmsDisabledEmbed(user, ex)).mentionRepliedUser(false).queue()
                return@exceptionally null
            }
    }

    private fun bookmarkEmbed(contentDisplay: String, guild: Guild, author: User, timeCreated: OffsetDateTime, messageId: String, user: User): EmbedBuilder {
        return EmbedBuilder()
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
    }

    private fun dmsDisabledEmbed(user: User, throwable: Throwable): MessageEmbed {
        return ResponseHelper.generateFailureEmbed(
            user,
            "Exception Occurred",
            "**Exception:** ${throwable.localizedMessage}"
        )
    }

}