package ru.sigsegv.lab5.command;

/**
 * `exit` command handler
 */
public class CmdExit implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        ctx.exit();
    }

    @Override
    public String getDescription() {
        return "Self-explanatory.";
    }
}
