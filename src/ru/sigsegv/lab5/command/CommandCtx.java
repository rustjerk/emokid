package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.Database;
import ru.sigsegv.lab5.util.ArgsSplitter;

/**
 * Command context (provides references to the database and input)
 */
public class CommandCtx {
    private final CommandInput input;
    private final CommandRegistry registry;
    private final Database database;

    /**
     * @param input command input
     * @param registry command registry (stores command handlers)
     * @param database the database (stores music bands)
     */
    public CommandCtx(CommandInput input, CommandRegistry registry, Database database) {
        this.input = input;
        this.registry = registry;
        this.database = database;
    }

    private boolean exitFlag;

    /**
     * @return command input
     */
    public CommandInput getInput() {
        return input;
    }

    /**
     * @return command registry
     */
    public CommandRegistry getRegistry() {
        return registry;
    }

    /**
     * @return the database
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Prints a string to the output (without newline).
     * @param str string to print
     */
    public void print(String str) {
        System.out.print(str);
    }

    /**
     * Prints a string to the output (with newline).
     * @param str string to print
     */
    public void println(String str) {
        System.out.println(str);
    }

    /**
     * Prints a formatted string to the output.
     * @param fmt format string
     * @param args formatting arguments
     */
    public void printf(String fmt, Object... args) {
        System.out.printf(fmt, args);
    }

    /**
     * Sets the exit flag to true, which controls the execution loop.
     */
    public void exit() {
        exitFlag = true;
    }

    /**
     * Runs the loop, which processes commands until exit flag is set to true.
     */
    public void runRepl() {
        while (!exitFlag) {
            String line = input.readLine("> ");
            if (line == null) {
                break;
            }

            String[] args = ArgsSplitter.splitArgs(line);
            if (args.length == 0)
                continue;

            String cmdName = args[0];
            if (cmdName.isEmpty())
                continue;

            Command cmd = registry.getCommand(cmdName);
            if (cmd == null) {
                System.out.println("No such command: " + cmdName);
                continue;
            }

            try {
                cmd.execute(this, args);
            } catch (Exception e) {
                System.out.println("Error occurred while executing command " + cmdName);
                e.printStackTrace();
            }
        }
    }
}
