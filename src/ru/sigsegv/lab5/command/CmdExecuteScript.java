package ru.sigsegv.lab5.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * `execute_script` command handler
 */
public class CmdExecuteScript implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        try {
            Scanner scanner = new Scanner(new File(args[1]));
            CommandInput input = new ScriptInput(scanner);
            CommandCtx newCtx = new CommandCtx(input, ctx.getRegistry(), ctx.getDatabase());
            newCtx.runRepl();
        } catch (FileNotFoundException e) {
            ctx.println("File not found.");
        }
    }

    @Override
    public String getDescription() {
        return "Executes a script.";
    }

    @Override
    public String getHelpMessage() {
        return "Executes a script.\nSyntax: execute_script <path>";
    }
}
