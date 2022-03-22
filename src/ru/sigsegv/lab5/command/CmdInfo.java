package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.Database;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * `info` command handler
 */
public class CmdInfo implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Database db = ctx.getDatabase();
        ctx.printf("Collection type: %s%n", db.getType());
        ctx.printf("Collection size: %s%n", db.getMusicBandSet().size());
        ctx.printf("Initialized at %s%n", db.getInitializationTime().format(ISO_LOCAL_DATE_TIME));
    }

    @Override
    public String getDescription() {
        return "Shows collection information.";
    }
}
