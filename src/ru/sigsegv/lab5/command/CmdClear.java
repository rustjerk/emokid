package ru.sigsegv.lab5.command;

/**
 * `clear` command handler
 */
public class CmdClear implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        ctx.getDatabase().getMusicBandSet().clear();
        System.out.println("Collection cleared.");
    }

    @Override
    public String getDescription() {
        return "Clears the collection.";
    }
}
