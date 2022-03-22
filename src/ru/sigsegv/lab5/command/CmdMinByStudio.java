package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/**
 * `min_by_studio` command handler
 */
public class CmdMinByStudio implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        Optional<MusicBand> band = set.stream().min(Comparator.comparing(MusicBand::getStudio));
        if (band.isPresent()) ctx.print(band.toString());
    }

    @Override
    public String getDescription() {
        return "Prints the music band with smallest studio.";
    }
}
