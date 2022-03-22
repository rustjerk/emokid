package ru.sigsegv.lab5.command;

/**
 * Command handler
 */
public interface Command {
    /**
     * Executes the command.
     * @param ctx command context (provides references to the database and input)
     * @param args command args (with 0 being the command itself)
     */
    void execute(CommandCtx ctx, String[] args);

    /**
     * @return a short command description (single line)
     */
    default String getDescription() {
        return "No description.";
    }

    /**
     * @return message shown in the `help` command (can span multiple lines)
     */
    default String getHelpMessage() {
        return getDescription();
    }
}
