package ru.sigsegv.emokid.client;

import ru.sigsegv.emokid.common.Command;
import ru.sigsegv.emokid.common.Response;
import ru.sigsegv.emokid.common.model.Credentials;
import ru.sigsegv.emokid.common.model.DatabaseInfo;
import ru.sigsegv.emokid.common.model.MusicBand;
import ru.sigsegv.emokid.common.model.Studio;
import ru.sigsegv.emokid.common.serde.DeserializeException;
import ru.sigsegv.emokid.common.serde.SerDe;
import ru.sigsegv.emokid.common.util.ArgsSplitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class CommandHandler {
    private Client client;
    private final Client clientTCP;
    private final Client clientUDP;

    private CommandContext ctx;

    private final HashMap<String, CommandEntry> commands = gatherCommands();

    public CommandHandler(SocketAddress serverAddress, CommandContext ctx) throws IOException {
        clientTCP = new ClientTCP(serverAddress);
        clientUDP = new ClientUDP(serverAddress);
        client = clientTCP;
        this.ctx = ctx;
    }

    public CommandHandler(SocketAddress serverAddress) throws IOException {
        this(serverAddress, new ConsoleCommandContext());
    }

    @Handler(command = "add",
            description = "Adds a single music band to the collection.")
    private void commandAdd(String[] args) throws IOException {
        var band = enterMusicBand();
        if (band == null) return;

        printResponse(client.request(Command.ADD, band));
    }

    @Handler(command = "add_if_max",
            description = "Adds a single music band to the collection, if it's greater than all the other bands.")
    private void commandAddIfMax(String[] args) throws IOException {
        var band = enterMusicBand();
        if (band == null) return;

        printResponse(client.request(Command.ADD_IF_MAX, band));
    }

    @Handler(command = "clear",
            description = "Clears the collection.")
    private void commandClear(String[] args) throws IOException {
        printResponse(client.request(Command.CLEAR));
    }

    @Handler(command = "count_greater_than_studio",
            description = "Counts music bands with studio greater than the given one.")
    private void commandCountGreaterThanStudio(String[] args) throws IOException {
        Studio studio;

        try {
            studio = SerDe.deserialize(new CommandDeserializer(ctx), Studio.class);
        } catch (DeserializeException e) {
            ctx.printf("Erroneous input: %s%n", e.getMessage());
            return;
        }

        Response<Long> response = client.request(Command.COUNT_GREATER_THAN_STUDIO, studio);
        printResponse(response);
        if (response.isSuccess()) {
            ctx.println(response.payload().toString());
        }
    }

    @Handler(command = "execute_script",
            description = "Executes a script.",
            helpMessage = "Executes a script.\nSyntax: execute_script <path>")
    private void commandExecuteScript(String[] args) {
        try {
            var scanner = new Scanner(new File(args[1]));
            CommandContext newCtx = new ScriptCommandContext(scanner);
            var oldCtx = ctx;
            ctx = newCtx;

            try {
                runREPL();
            } catch (Exception e) {
                ctx.println("Script error: " + getRootExceptionMessage(e));
            }

            ctx = oldCtx;
        } catch (FileNotFoundException e) {
            ctx.println("File not found.");
        }
    }

    @Handler(command = "exit")
    private void commandExit(String[] args) {
        ctx.stop();
    }

    @Handler(command = "help",
            description = "Prints command listing and help.",
            helpMessage = "Prints command list and help.\nSyntax: help [cmd]",
            requiresAuth = false)
    private void commandHelp(String[] args) {
        if (args.length < 2) {
            ctx.println("Available commands: ");
            var commandNames = commands.keySet().stream()
                    .filter(cmdName -> !commands.get(cmdName).requiresAuth() || client.isAuthenticated())
                    .sorted().toList();
            var maxLength = commandNames.stream().mapToInt(String::length).max().orElse(0);

            for (var commandName : commandNames) {
                var command = commands.get(commandName);
                var padding = String.join("", Collections.nCopies(maxLength - commandName.length(), " "));
                ctx.printf("%s:%s %s%n", commandName, padding, command.getDescription());
            }
        } else {
            var cmd = commands.get(args[1]);
            if (cmd == null) {
                ctx.printf("No such command: " + args[1]);
                return;
            }

            ctx.printf("%s: %s%n", args[1], cmd.getHelpMessage());
        }
    }

    @Handler(command = "info",
            description = "Shows collection information.")
    private void commandInfo(String[] args) throws IOException {
        Response<DatabaseInfo> response = client.request(Command.INFO);
        printResponse(response);
        if (response.isError()) return;

        var info = response.payload();
        ctx.println("Collection type: " + info.type());
        ctx.println("Collection size: " + info.size());
        ctx.println("Initialized at " + info.initializationTime().format(ISO_LOCAL_DATE_TIME));
    }

    @Handler(command = "login",
            description = "Login.",
            requiresAuth = false)
    private void commandLogin(String[] args) throws IOException {
        var credentials = enterCredentials();
        Response<String> response = client.request(Command.LOGIN, credentials);
        printResponse(response);
        if (response.isSuccess()) {
            clientTCP.setAuthToken(response.payload());
            clientUDP.setAuthToken(response.payload());
        }
    }

    @Handler(command = "logout",
            description = "Logout.")
    private void commandLogout(String[] args) {
        clientTCP.setAuthToken(null);
        clientUDP.setAuthToken(null);
    }

    @Handler(command = "min_by_studio",
            description = "Prints the music band with smallest studio.")
    private void commandMinByStudio(String[] args) throws IOException {
        Response<MusicBand> response = client.request(Command.MIN_BY_STUDIO);
        printResponse(response);
        if (response.isError()) return;

        if (response.payload() != null) {
            ctx.println(response.payload().toString());
        } else {
            ctx.println("No studios.");
        }
    }

    @Handler(command = "print_field_ascending_studio",
            description = "Prints the studios in ascending order.")
    private void commandPrintFieldAscendingStudio(String[] args) throws IOException {
        Response<List<Studio>> response = client.request(Command.PRINT_FIELD_ASCENDING_STUDIO);
        printResponse(response);
        if (response.isError()) return;

        for (var studio : response.payload()) {
            ctx.println(studio.toString());
        }
    }

    @Handler(command = "protocol",
            description = "Changes the server communication protocol.")
    private void commandProtocol(String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("tcp")) {
            client = clientTCP;
            ctx.println("Success");
        } else if (args.length == 2 && args[1].equalsIgnoreCase("udp")) {
            client = clientUDP;
            ctx.println("Success");
        } else if (args.length == 1) {
            ctx.println("Current protocol: " + (client == clientTCP ? "TCP." : "UDP."));
        } else {
            ctx.println("Invalid syntax.");
        }
    }

    @Handler(command = "register",
            description = "Register.",
            requiresAuth = false)
    private void commandRegister(String[] args) throws IOException {
        var credentials = enterCredentials();
        var response = client.request(Command.REGISTER, credentials);
        printResponse(response);
    }

    @Handler(command = "remove_by_id",
            description = "Removes a music band by id.",
            helpMessage = "Removes a music band by id.\nSyntax: remove_by_id <id>")
    private void commandRemoveById(String[] args) throws IOException {
        var id = parseLongArg(args, 1);
        if (id == null) return;

        Response<Boolean> response = client.request(Command.REMOVE_BY_ID, id);
        printResponse(response);

        if (response.isSuccess() && !response.payload()) {
            ctx.println("No band with specified id and owned by current user.");
        }
    }

    @Handler(command = "remove_greater",
            description = "Removes music bands greater than the given one.")
    private void commandRemoveGreater(String[] args) throws IOException {
        var band = enterMusicBand();
        if (band == null) return;

        Response<Boolean> response = client.request(Command.REMOVE_GREATER, band);
        printResponse(response);
    }

    @Handler(command = "remove_lower",
            description = "Removes music bands lower than the given one.")
    private void commandRemoveLower(String[] args) throws IOException {
        var band = enterMusicBand();
        if (band == null) return;

        Response<Boolean> response = client.request(Command.REMOVE_LOWER, band);
        printResponse(response);
    }

    @Handler(command = "show",
            description = "Prints collection elements.")
    private void commandShow(String[] args) throws IOException {
        Response<List<MusicBand>> response = client.request(Command.SHOW);
        printResponse(response);
        if (response.isError()) return;

        for (var band : response.payload()) {
            ctx.println(band.toString());
        }
    }

    @Handler(command = "update",
            description = "Updates a music band by id.",
            helpMessage = "Updates a music band by id.\nSyntax: update <id>")
    private void commandUpdate(String[] args) throws IOException {
        var id = parseLongArg(args, 1);
        if (id == null) return;

        var band = enterMusicBand();
        if (band == null) return;
        band = band.withId(id);

        Response<Boolean> response = client.request(Command.UPDATE, band);
        printResponse(response);
        if (response.isSuccess() && !response.payload()) {
            ctx.println("No such ID.");
        }
    }

    /**
     * Parses a command argument to long.
     *
     * @param args command args (with 0 being the command itself)
     * @param idx  argument index
     * @return long value of the idx`th argument, or null if it's invalid
     */
    public Long parseLongArg(String[] args, int idx) {
        try {
            return Long.parseLong(args[idx]);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            ctx.println("Invalid syntax");
            return null;
        }
    }

    private MusicBand enterMusicBand() {
        try {
            return SerDe.deserialize(new CommandDeserializer(ctx), MusicBand.class);
        } catch (DeserializeException e) {
            ctx.printf("Erroneous input: %s%n", e.getMessage());
            return null;
        }
    }

    private Credentials enterCredentials() {
        var username = ctx.readLine("Username: ");
        var password = ctx.readLine("Password: ");
        return new Credentials(username, password);
    }

    private void printResponse(Response<?> response) {
        if (response.isError()) {
            ctx.println("Error: " + response.payload());
        } else {
            ctx.println("Success.");
        }
    }

    public void runREPL() {
        while (ctx.isRunning()) {
            var line = ctx.readLine(ctx.defaultPrompt());
            if (line == null) break;

            executeLine(line);
        }
    }

    private void executeLine(String line) {
        var args = ArgsSplitter.splitArgs(line);
        if (args.length == 0)
            return;

        executeCommand(args);
    }

    private void executeCommand(String[] args) {
        var command = commands.get(args[0].toLowerCase());
        if (command == null) {
            ctx.println("No such command: " + args[0]);
            return;
        }

        if (command.requiresAuth() && !client.isAuthenticated()) {
            ctx.println("Unauthenticated.");
            return;
        }

        try {
            command.execute(args);
            ctx.flush();
        } catch (Exception e) {
            ctx.println("Error: " + getRootExceptionMessage(e));
//            e.printStackTrace();
        }
    }

    private HashMap<String, CommandEntry> gatherCommands() {
        var commandMethods = new HashMap<String, CommandEntry>();

        for (var method : getClass().getDeclaredMethods()) {
            var annotation = method.getAnnotation(Handler.class);
            if (annotation == null) continue;

            if (method.getParameterCount() != 1 ||
                    !method.getParameters()[0].getType().isAssignableFrom(String[].class)) {
                throw new RuntimeException("Command handler should receive one String[] args argument.");
            }

            var handler = new CommandEntry() {
                @Override
                public void execute(String[] args) {
                    try {
                        method.setAccessible(true);
                        method.invoke(CommandHandler.this, (Object) args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String getDescription() {
                    return annotation.description();
                }

                @Override
                public String getHelpMessage() {
                    var helpMessage = annotation.helpMessage();
                    if (helpMessage.isEmpty())
                        return getDescription();
                    return helpMessage;
                }

                @Override
                public boolean requiresAuth() {
                    return annotation.requiresAuth();
                }
            };

            commandMethods.put(annotation.command(), handler);
        }

        return commandMethods;
    }

    private static String getRootExceptionMessage(Throwable e) {
        var cause = Stream.iterate(e, Throwable::getCause)
                .filter(element -> element.getCause() == null)
                .findFirst()
                .orElse(e);
        var message = cause.getMessage();
        return message == null ? cause.toString() : message;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Handler {
        String command();

        String description() default "Self-explanatory.";

        String helpMessage() default "";

        boolean requiresAuth() default true;
    }

    private interface CommandEntry {
        void execute(String[] args);

        String getDescription();

        String getHelpMessage();

        boolean requiresAuth();
    }
}
