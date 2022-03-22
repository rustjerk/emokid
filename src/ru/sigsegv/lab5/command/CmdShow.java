package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.Database;
import ru.sigsegv.lab5.model.MusicBand;

/**
 * `show` command handler
 */
public class CmdShow implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Database db = ctx.getDatabase();
        for (MusicBand band : db.getMusicBandSet()) {
            ctx.println(band.toString());
        }
    }

    @Override
    public String getDescription() {
        return "Prints collection elements.";
    }
}
