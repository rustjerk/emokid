package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;

import java.util.Comparator;
import java.util.Set;

/**
 * `add_if_max` command handler
 */
public class CmdAddIfMax implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        MusicBand band = CmdUtils.enterMusicBand(ctx);
        if (band == null) return;

        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        MusicBand max = set.stream().max(Comparator.naturalOrder()).orElse(band);
        if (max.compareTo(band) <= 0) {
            set.add(band);
        }
    }

    @Override
    public String getDescription() {
        return "Adds a single music band to the collection, if it's greater than all the other bands.";
    }
}
