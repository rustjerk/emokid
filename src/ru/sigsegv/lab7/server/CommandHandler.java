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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandHandler implements RequestHandler {
    private static final Logger logger = Logger.getLogger(CommandHandler.class.getSimpleName());

    private final Database database;

    private final HashMap<Command, RequestHandler> commands = gatherCommands();

    public CommandHandler(Database database) {
        this.database = database;
    }

    @Handler(Command.ADD)
    private Response<?> commandAdd(MusicBand band) {
        band.setId(IdGenerator.generateId());
        database.getMusicBandSet().add(band);
        return Response.success();
    }

    @Handler(Command.ADD_IF_MAX)
    private Response<?> commandAddIfMax(MusicBand band) {
        Set<MusicBand> set = database.getMusicBandSet();
        MusicBand max = set.stream().max(Comparator.naturalOrder()).orElse(band);
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
        Set<MusicBand> set = database.getMusicBandSet();
        long count = set.stream().filter(b -> b.getStudio() != null && b.getStudio().compareTo(studio) > 0).count();
        return Response.success(count);
    }

    @Handler(Command.INFO)
    private Response<?> commandInfo() {
        DatabaseInfo info = new DatabaseInfo();
        info.type = database.getType();
        info.size = database.getMusicBandSet().size();
        info.initializationTime = database.getInitializationTime();
        return Response.success(info);
    }

    @Handler(Command.MIN_BY_STUDIO)
    private Response<?> commandMinByStudio() {
        Set<MusicBand> set = database.getMusicBandSet();
        MusicBand band = set.stream()
                .filter(b -> b.getStudio() != null)
                .min(Comparator.comparing(MusicBand::getStudio))
                .orElse(null);
        return Response.success(band);
    }

    @Handler(Command.PRINT_FIELD_ASCENDING_STUDIO)
    private Response<?> commandPrintFieldAscendingStudio() {
        Set<MusicBand> set = database.getMusicBandSet();
        List<Studio> studios = set.stream()
                .map(MusicBand::getStudio)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        return Response.success(studios);
    }

    @Handler(Command.REMOVE_BY_ID)
    private Response<?> commandRemoveById(Long id) {
        Set<MusicBand> set = database.getMusicBandSet();
        return Response.success(set.removeIf(b -> b.getId() == id));
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
        List<MusicBand> bands = database.getMusicBandSet().stream().sorted().collect(Collectors.toList());
        return Response.success(bands);
    }

    @Handler(Command.UPDATE)
    private Response<?> commandUpdate(MusicBand band) {
        Set<MusicBand> set = database.getMusicBandSet();
        if (!set.removeIf(b -> b.getId() == band.getId()))
            return Response.success(false);
        set.add(band);
        return Response.success(true);
    }

    @Override
    public Response<?> handle(Request<?> request) {
        logger.info(request.getCommand().toString());
        if (request.getArgument() != null)
            logger.info(request.getArgument().toString());

        RequestHandler handler = commands.get(request.getCommand());
        if (handler == null)
            return Response.invalidRequest();
        return handler.handle(request);
    }

    private HashMap<Command, RequestHandler> gatherCommands() {
        HashMap<Command, RequestHandler> commandMethods = new HashMap<>();

        for (Method method : getClass().getDeclaredMethods()) {
            Handler annotation = method.getAnnotation(Handler.class);
            if (annotation == null) continue;

            RequestHandler handler;

            switch (method.getParameterCount()) {
                case 0:
                    handler = request -> {
                        if (request.getArgument() != null)
                            return Response.invalidRequest();

                        try {
                            return (Response<?>) method.invoke(this);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    break;
                case 1:
                    Parameter parameter = method.getParameters()[0];
                    handler = request -> {
                        Object argument = request.getArgument();
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
                    break;
                default:
                    throw new UnsupportedOperationException("command handlers cannot receive more than 1 argument");
            }

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
