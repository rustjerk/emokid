package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;
import ru.sigsegv.lab5.model.Studio;
import ru.sigsegv.lab5.serde.CommandDeserializer;
import ru.sigsegv.lab5.serde.DeserializeException;

import java.util.Set;

/**
 * `count_greater_than_studio` command handler
 */
public class CmdCountGreaterThanStudio implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Studio studio = new Studio();

        try {
            studio.deserialize(new CommandDeserializer(ctx.getInput()));
        } catch (DeserializeException e) {
            ctx.printf("Erroneous input: %s%n", e.getMessage());
            return;
        }

        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        long count = set.stream().filter(b -> b.getStudio().compareTo(studio) > 0).count();
        ctx.printf("Count: %d.", count);
    }

    @Override
    public String getDescription() {
        return "Counts music bands with studio greater than the given one.";
    }
}
