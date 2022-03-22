package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;

/**
 * `add` command handler
 */
public class CmdAdd implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        MusicBand band = CmdUtils.enterMusicBand(ctx);
        if (band == null) return;
        ctx.getDatabase().getMusicBandSet().add(band);
    }

    @Override
    public String getDescription() {
        return "Adds a single music band to the collection.";
    }
}
