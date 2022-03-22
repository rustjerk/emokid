package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;

import java.util.Set;

/**
 * `remove_greater` command handler
 */
public class CmdRemoveGreater implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        MusicBand band = CmdUtils.enterMusicBand(ctx);
        if (band == null) return;

        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        set.removeIf(b -> b.compareTo(band) > 0);
    }

    @Override
    public String getDescription() {
        return "Removes music bands greater than the given one.";
    }
}
