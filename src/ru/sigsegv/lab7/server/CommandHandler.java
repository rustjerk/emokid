package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.Command;
import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;
import ru.sigsegv.lab7.common.model.DatabaseInfo;
import ru.sigsegv.lab7.common.model.MusicBand;
import ru.sigsegv.lab7.common.model.Studio;
import ru.sigsegv.lab7.common.util.IdGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandHandler implements RequestHandler {
    private static final Logger LOG = Logger.getLogger(CommandHandler.class.getSimpleName());

    private final Database database;

    private final HashMap<Command, RequestHandler> commands = gatherCommands();

    public CommandHandler(Database database) {
        this.database = database;
    }

    @Handler(Command.ADD)
    private Response<?> commandAdd(MusicBand band) {
        band = band.withId(IdGenerator.generateId());
        database.getMusicBandSet().add(band);
        return Response.success();
    }

    @Handler(Command.ADD_IF_MAX)
    private Response<?> commandAddIfMax(MusicBand band) {
        var set = database.getMusicBandSet();
        var max = set.stream().max(Comparator.naturalOrder()).orElse(band);
        if (max.compareTo(band) <= 0) {
            set.add(band);
        }

        return Response.success();
    }

    @Handler(Command.CLEAR)
    private Response<?> commandClear() {
        database.getMusicBandSet().clear();
        return Response.success();
    }

    @Handler(Command.COUNT_GREATER_THAN_STUDIO)
    private Response<?> commandCountGreaterThanStudio(Studio studio) {
        var set = database.getMusicBandSet();
        var count = set.stream().filter(b -> b.studio() != null && b.studio().compareTo(studio) > 0).count();
        return Response.success(count);
    }

    @Handler(Command.INFO)
    private Response<?> commandInfo() {
        var info = new DatabaseInfo(database.getType(),
                database.getMusicBandSet().size(),
                database.getInitializationTime());
        return Response.success(info);
    }

    @Handler(Command.MIN_BY_STUDIO)
    private Response<?> commandMinByStudio() {
        Set<MusicBand> set = database.getMusicBandSet();
        var band = set.stream()
                .filter(b -> b.studio() != null)
                .min(Comparator.comparing(MusicBand::studio))
                .orElse(null);
        return Response.success(band);
    }

    @Handler(Command.PRINT_FIELD_ASCENDING_STUDIO)
    private Response<?> commandPrintFieldAscendingStudio() {
        Set<MusicBand> set = database.getMusicBandSet();
        var studios = set.stream()
                .map(MusicBand::studio)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        return Response.success(studios);
    }

    @Handler(Command.REMOVE_BY_ID)
    private Response<?> commandRemoveById(Long id) {
        Set<MusicBand> set = database.getMusicBandSet();
        return Response.success(set.removeIf(b -> b.id() == id));
    }

    @Handler(Command.REMOVE_GREATER)
    private Response<?> commandRemoveGreater(MusicBand band) {
        Set<MusicBand> set = database.getMusicBandSet();
        set.removeIf(b -> b.compareTo(band) > 0);
        return Response.success();
    }

    @Handler(Command.REMOVE_LOWER)
    private Response<?> commandRemoveLower(MusicBand band) {
        Set<MusicBand> set = database.getMusicBandSet();
        set.removeIf(b -> b.compareTo(band) < 0);
        return Response.success();
    }

    @Handler(Command.SHOW)
    private Response<?> commandShow() {
        var bands = database.getMusicBandSet().stream().sorted().collect(Collectors.toList());
        return Response.success(bands);
    }

    @Handler(Command.UPDATE)
    private Response<?> commandUpdate(MusicBand band) {
        Set<MusicBand> set = database.getMusicBandSet();
        if (!set.removeIf(b -> b.id() == band.id()))
            return Response.success(false);
        set.add(band);
        return Response.success(true);
    }

    @Override
    public Response<?> handle(Request<?> request) {
        LOG.info("Received command: " + request.getCommand());
        if (request.getArgument() != null)
            LOG.info("Argument: " + request.getArgument());

        var handler = commands.get(request.getCommand());
        if (handler == null)
            return Response.invalidRequest();

        synchronized (database) {
            return handler.handle(request);
        }
    }

    private HashMap<Command, RequestHandler> gatherCommands() {
        var commandMethods = new HashMap<Command, RequestHandler>();

        for (var method : getClass().getDeclaredMethods()) {
            var annotation = method.getAnnotation(Handler.class);
            if (annotation == null) continue;

            RequestHandler handler = switch (method.getParameterCount()) {
                case 0 -> request -> {
                    if (request.getArgument() != null)
                        return Response.invalidRequest();

                    try {
                        return (Response<?>) method.invoke(this);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
                case 1 -> {
                    var parameter = method.getParameters()[0];
                    yield request -> {
                        var argument = request.getArgument();
                        if (argument != null) {
                            if (!parameter.getType().isAssignableFrom(argument.getClass())) {
                                return Response.invalidRequest();
                            }
                        }

                        try {
                            return (Response<?>) method.invoke(this, argument);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            return Response.exception(e);
                        }
                    };
                }
                default -> throw new UnsupportedOperationException("command handlers cannot receive more than 1 argument");
            };

            commandMethods.put(annotation.value(), handler);
        }

        return commandMethods;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Handler {
        Command value();
    }
}
