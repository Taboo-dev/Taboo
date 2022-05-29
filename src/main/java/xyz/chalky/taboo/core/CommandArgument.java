package xyz.chalky.taboo.core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArgument {

    private final String command;
    private final String[] args;

    public CommandArgument(@NotNull String argumentString, String prefix) {
        String[] argumentArray = argumentString.split("\\s+");
        this.command = argumentArray[0].substring(prefix.length());
        List<String> arguments = new ArrayList<>(Arrays.asList(argumentArray).subList(1, argumentArray.length));
        this.args = new String[arguments.size()];
        arguments.toArray(this.args);
    }

    public String getCommandName() {
        return command;
    }

    public String[] toStringArray() {
        return args;
    }

}
