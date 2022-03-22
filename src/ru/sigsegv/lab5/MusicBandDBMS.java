package ru.sigsegv.lab5;

import ru.sigsegv.lab5.command.CommandCtx;
import ru.sigsegv.lab5.command.CommandRegistry;
import ru.sigsegv.lab5.command.ConsoleInput;

import java.io.File;

/**
 * Music band database management system
 */
public class MusicBandDBMS {
    /**
     * Entry point
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        String inputPath = System.getenv("FILE");
        if (inputPath == null) inputPath = "save.json";

        Database database = new Database();
        try {
            database.load(new File(inputPath));
        } catch (Exception e) {
            System.out.println("Error loading input: " + e.getMessage());
        }

        CommandCtx ctx = new CommandCtx(new ConsoleInput(), CommandRegistry.withDefaultCommands(), database);
        ctx.runRepl();
    }
}
