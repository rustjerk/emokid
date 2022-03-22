package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;
import ru.sigsegv.lab5.serde.Serializer;
import ru.sigsegv.lab5.serde.json.JsonPrettySerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * `save` command handler
 */
public class CmdSave implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        String path = args.length < 2 ? System.getenv("FILE") : args[1];

        if (path == null) {
            ctx.println("File not specified.");
            return;
        }

        try {
            ctx.getDatabase().save(new File(path));
        } catch (IOException e) {
            ctx.printf("An error occurred: %s%n", e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Saves the collection in JSON.";
    }

    @Override
    public String getHelpMessage() {
        return "Saves the collection in JSON.\nSyntax: save [path]";
    }
}
