package dev.taboo.taboo.events

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import dev.minn.jda.ktx.Embed
import dev.taboo.taboo.commands.Settings
import dev.taboo.taboo.util.PropertiesManager
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.CommandType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.time.Instant

class Events {

    val manager = ReactiveEventManager()

    init {
        manager.on<ReadyEvent>()
            .next()
            .subscribe { event ->
                val jda = event.jda
                val actionLog = jda.getTextChannelById(PropertiesManager.actionLog)
                val guild = jda.getGuildById(PropertiesManager.guildId)
                LoggerFactory.getLogger("Ready").info("${jda.selfUser.asTag} is ready!")
                val readyEmbed = Embed {
                    title = "Taboo is now online!"
                    color = 0x9F90CF
                    timestamp = Instant.now()
                }
                actionLog!!.sendMessageEmbeds(readyEmbed).queue()
                guild!!.upsertCommand(CommandData(CommandType.MESSAGE_CONTEXT, "Bookmark")).queue()
            }
        manager.on<MessageReceivedEvent>()
            .next()
            .subscribe { event ->
                if (!event.isFromGuild) return@subscribe
                LoggerFactory.getLogger("Message Log").info(event.message.contentDisplay)
            }
        manager.on<GuildJoinEvent>()
            .next()
            .subscribe { event ->
                val guild = event.guild
                val guildId = guild.id
                val roles = guild.roles
                val defaultChannel = guild.defaultChannel
                val owner = guild.retrieveOwner().complete()
                val jda = event.jda
                val botOwner = jda.retrieveUserById(PropertiesManager.ownerId).complete()
                if (roles.stream().noneMatch { role ->
                        role.name == "Taboo Manager"
                }) {
                    guild.createRole()
                        .setName("Taboo Manager")
                        .setHoisted(false)
                        .setMentionable(false)
                        .flatMap { role ->
                            guild.addRoleToMember(owner!!, role)
                        }
                        .queue()
                }
                val joinEmbed = Embed {
                    title = "Hi! I'm Taboo!"
                    description =
                        """
                            I'm a bot that deletes harmful files or uploads them to Hastebin when they are uploaded by server members. I can do both at the same time too!
                            My prefix is simply mentioning me (or **t!**), or using Slash Commands, but it can be changed by running **@Taboo prefix <newPrefix>**.
                            To configure my settings, you need to have the **Taboo Manager** role, and you will need to run the **Settings** command.
                            By default, I have already given this role to the Server Owner, ${owner.user.asMention}
                        """.trimIndent()
                    color = 0x9F90CF
                    footer {
                        name = "Made with <3 by ${botOwner.asTag}"
                    }
                    timestamp = Instant.now()
                }
                defaultChannel!!.sendMessageEmbeds(joinEmbed).queue()
                transaction {
                    Settings.SetPrefix.Prefix.insertIgnore { table ->
                        table[Settings.SetPrefix.Prefix.guildId] = guildId
                        table[prefix] = "t!"
                    }
                }
            }
        /*
        manager.on<SlashCommandEvent>()
            .next()
            .subscribe { event ->
                val name = event.name
                val commandId = event.commandId
                val guild = event.guild ?: return@subscribe
                guild.deleteCommandById(commandId).queue {
                    Taboo.LOGGER.info("Removed command $name from ${guild.name}")
                }
            }
         */
    }

}