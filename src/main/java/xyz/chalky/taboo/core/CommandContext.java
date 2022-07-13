package xyz.chalky.taboo.core;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandContext {

    private final MessageReceivedEvent event;
    private final Command command;
    private final Member member;
    private final CommandArgument commandArgument;

    public CommandContext(MessageReceivedEvent event, Command command, Member member, CommandArgument commandArgument) {
        this.event = event;
        this.command = command;
        this.member = member;
        this.commandArgument = commandArgument;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public CommandArgument getArguments() {
        return commandArgument;
    }

    public Command getCommand() {
        return command;
    }

    public Member getMember() {
        return member;
    }

}
