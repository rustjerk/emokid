package ru.sigsegv.lab5.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Command registry (stores command handlers)
 */
public class CommandRegistry {
    private final Map<String, Command> commands = new LinkedHashMap<>();

    /**
     * Registers a command handler
     * @param name name of the command
     * @param command command handler
     */
    public void registerCommand(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Queries  a command handler by name.
     * @param name name of the command to query
     * @return command handler, or null if it doesn't exist
     */
    public Command getCommand(String name) {
        return commands.get(name);
    }

    /**
     * @return an immutable view of all available commands
     */
    public Map<String, Command> commands() {
        return Collections.unmodifiableMap(commands);
    }

    /**
     * @return command registry initialized with all the default commands
     */
    public static CommandRegistry withDefaultCommands() {
        CommandRegistry registry = new CommandRegistry();

        registry.registerCommand("help", new CmdHelp());
        registry.registerCommand("info", new CmdInfo());
        registry.registerCommand("show", new CmdShow());
        registry.registerCommand("add", new CmdAdd());
        registry.registerCommand("update", new CmdUpdate());
        registry.registerCommand("remove_by_id", new CmdRemoveById());
        registry.registerCommand("clear", new CmdClear());
        registry.registerCommand("save", new CmdSave());
        registry.registerCommand("execute_script", new CmdExecuteScript());
        registry.registerCommand("exit", new CmdExit());
        registry.registerCommand("add_if_max", new CmdAddIfMax());
        registry.registerCommand("remove_greater", new CmdRemoveGreater());
        registry.registerCommand("remove_lower", new CmdRemoveLower());
        registry.registerCommand("min_by_studio", new CmdMinByStudio());
        registry.registerCommand("count_greater_than_studio", new CmdCountGreaterThanStudio());
        registry.registerCommand("print_field_ascending_studio", new CmdPrintFieldAscendingStudio());

        return registry;
    }
}
