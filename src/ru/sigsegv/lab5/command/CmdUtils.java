package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;
import ru.sigsegv.lab5.serde.CommandDeserializer;
import ru.sigsegv.lab5.serde.DeserializeException;

/**
 * Various utilities used by command handlers
 */
public class CmdUtils {
    /**
     * Parses a command argument to long.
     * @param ctx command context (provides references to the database and input)
     * @param args command args (with 0 being the command itself)
     * @param idx argument index
     * @return long value of the idx`th argument, or null if it's invalid
     */
    public static Long parseLongArg(CommandCtx ctx, String[] args, int idx) {
        try {
           return Long.parseLong(args[idx]);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            ctx.println("Invalid syntax");
            return null;
        }
    }

    /**
     * Asks the user to enter a music band, line by line.
     * @param ctx command context (provides references to the database and input)
     * @return music band instance, or null if it could not be parsed
     */
    public static MusicBand enterMusicBand(CommandCtx ctx) {
        try {
            MusicBand band = new MusicBand();
            band.deserialize(new CommandDeserializer(ctx.getInput()));
            return band;
        } catch (DeserializeException e) {
            ctx.printf("Erroneous input: %s%n", e.getMessage());
            return null;
        }
    }
}
