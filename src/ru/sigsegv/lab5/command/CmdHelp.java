package ru.sigsegv.lab5.command;

import java.util.Collections;
import java.util.Map;

/**
 * `help` command handler
 */
public class CmdHelp implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Map<String, Command> commands = ctx.getRegistry().commands();

        if (args.length < 2) {
            ctx.println("Available commands: ");

            int maxLength = commands.keySet().stream().mapToInt(String::length).max().orElse(0);

            for (Map.Entry<String, Command> entry : commands.entrySet()) {
                String name = entry.getKey();
                Command cmd = entry.getValue();
                String padding = String.join("", Collections.nCopies(maxLength - name.length(), " "));
                ctx.printf("%s:%s %s%n", name, padding, cmd.getDescription());
            }
        } else if (commands.containsKey(args[1])) {
            Command cmd = commands.get(args[1]);
            ctx.printf("%s: %s%n", args[1], cmd.getHelpMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Prints command list and help.";
    }

    @Override
    public String getHelpMessage() {
        return "Prints command list and help.\nSyntax: help [cmd]";
    }
}
