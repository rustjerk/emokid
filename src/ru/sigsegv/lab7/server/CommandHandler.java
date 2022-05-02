package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.Command;
import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;
import ru.sigsegv.lab7.common.model.Credentials;
import ru.sigsegv.lab7.common.model.DatabaseInfo;
import ru.sigsegv.lab7.common.model.MusicBand;
import ru.sigsegv.lab7.common.model.Studio;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandHandler implements RequestHandler {
    private static final Logger LOG = Logger.getLogger(CommandHandler.class.getSimpleName());

    private final Database database;

    private final HashMap<Command, CommandEntry> commands = gatherCommands();

    public CommandHandler(Database database) {
        this.database = database;
    }

    @Handler(Command.ADD)
    private Response<?> commandAdd(CommandContext ctx, MusicBand band) throws SQLException {
        try (var conn = database.getConnection()) {
            var creationDate = ZonedDateTime.now();

            var stmt = conn.prepareStatement(
                    "insert into music_bands (owner, name, coord_x, coord_y," +
                            "creation_date, num_participants, description, " +
                            "genre, studio_name, studio_address\n) " +
                            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id");

            stmt.setString(1, ctx.currentUser);
            stmt.setString(2, band.name());
            stmt.setDouble(3, band.coordinates().x());
            stmt.setLong(4, band.coordinates().y());
            stmt.setObject(5, creationDate.toOffsetDateTime());
            stmt.setInt(6, band.numberOfParticipants());
            stmt.setString(7, band.description());
            stmt.setString(8, band.genre() == null ? null : band.genre().name());
            stmt.setString(9, band.studio() == null ? null : band.studio().name());
            stmt.setString(10, band.studio() == null ? null : band.studio().address());

            var res = stmt.executeQuery();
            res.next();
            var id = res.getLong(1);
            band = band.withParams(id, ctx.currentUser, creationDate);
            database.getMusicBandSet().add(band);
        }

        return Response.success();
    }

    @Handler(Command.ADD_IF_MAX)
    private Response<?> commandAddIfMax(CommandContext ctx, MusicBand band) throws SQLException {
        var set = database.getMusicBandSet();
        var max = set.stream().max(Comparator.naturalOrder()).orElse(band);
        if (max.compareTo(band) > 0) return Response.success();
        return commandAdd(ctx, band);
    }

    @Handler(Command.CLEAR)
    private Response<?> commandClear(CommandContext ctx) throws SQLException {
        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("delete from music_bands where owner = ?");
            stmt.setString(1, ctx.currentUser);
            stmt.execute();

            database.getMusicBandSet().removeIf(b -> b.owner().equals(ctx.currentUser));
            return Response.success();
        }
    }

    @Handler(Command.COUNT_GREATER_THAN_STUDIO)
    private Response<?> commandCountGreaterThanStudio(CommandContext ctx, Studio studio) {
        var set = database.getMusicBandSet();
        var count = set.stream().filter(b -> b.studio() != null && b.studio().compareTo(studio) > 0).count();
        return Response.success(count);
    }

    @Handler(Command.INFO)
    private Response<?> commandInfo(CommandContext ctx) {
        var info = new DatabaseInfo(database.getType(),
                database.getMusicBandSet().size(),
                database.getInitializationTime());
        return Response.success(info);
    }

    @Handler(value = Command.LOGIN, requiresAuth = false)
    private Response<?> commandLogin(CommandContext ctx, Credentials credentials) throws SQLException {
        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("select salt from users where username = ?");
            stmt.setString(1, credentials.username());

            var res = stmt.executeQuery();
            if (!res.next()) return Response.noSuchUser();

            var salt = res.getString(1);
            var password = hashPassword(credentials.password(), salt);

            stmt = conn.prepareStatement("select * from users where username = ? and password = ?");
            stmt.setString(1, credentials.username());
            stmt.setString(2, password);
            if (!stmt.executeQuery().next()) return Response.invalidPassword();

            var token = generateToken();
            stmt = conn.prepareStatement("insert into auth_tokens values (?, ?) on conflict (username) do update set token = excluded.token");
            stmt.setString(1, credentials.username());
            stmt.setString(2, token);
            stmt.execute();

            return Response.success(token);
        }
    }

    @Handler(Command.MIN_BY_STUDIO)
    private Response<?> commandMinByStudio(CommandContext ctx) {
        Set<MusicBand> set = database.getMusicBandSet();
        var band = set.stream()
                .filter(b -> b.studio() != null)
                .min(Comparator.comparing(MusicBand::studio))
                .orElse(null);
        return Response.success(band);
    }

    @Handler(Command.PRINT_FIELD_ASCENDING_STUDIO)
    private Response<?> commandPrintFieldAscendingStudio(CommandContext ctx) {
        Set<MusicBand> set = database.getMusicBandSet();
        var studios = set.stream()
                .map(MusicBand::studio)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        return Response.success(studios);
    }

    @Handler(value = Command.REGISTER, requiresAuth = false)
    private Response<?> commandRegister(CommandContext ctx, Credentials credentials) throws SQLException {
        try (var conn = database.getConnection()) {
            var salt = generateSalt();
            var password = hashPassword(credentials.password(), salt);
            var stmt = conn.prepareStatement("insert into users values (?, ?, ?)");
            stmt.setString(1, credentials.username());
            stmt.setString(2, password);
            stmt.setString(3, salt);
            try {
                stmt.execute();
                return Response.success();
            } catch (SQLException e) {
                return Response.usernameTaken();
            }
        }
    }

    @Handler(Command.REMOVE_BY_ID)
    private Response<?> commandRemoveById(CommandContext ctx, Long id) throws SQLException {
        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("delete from music_bands where id = ? and owner = ?");
            stmt.setLong(1, id);
            stmt.setString(2, ctx.currentUser);
            stmt.executeUpdate();

            Set<MusicBand> set = database.getMusicBandSet();
            var deleted = set.removeIf(b -> b.id() == id && b.owner().equals(ctx.currentUser));

            return Response.success(deleted);
        }
    }

    @Handler(Command.REMOVE_GREATER)
    private Response<?> commandRemoveGreater(CommandContext ctx, MusicBand band) throws SQLException {
        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("delete from music_bands where owner = ? and name > ?");
            stmt.setString(1, ctx.currentUser);
            stmt.setString(2, band.name());
            stmt.executeUpdate();

            Set<MusicBand> set = database.getMusicBandSet();
            var deleted = set.removeIf(b -> b.owner().equals(ctx.currentUser)
                    && b.name().compareTo(band.name()) > 0);

            return Response.success(deleted);
        }
    }

    @Handler(Command.REMOVE_LOWER)
    private Response<?> commandRemoveLower(CommandContext ctx, MusicBand band) throws SQLException {
        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("delete from music_bands where owner = ? and name < ?");
            stmt.setString(1, ctx.currentUser);
            stmt.setString(2, band.name());
            stmt.executeUpdate();

            Set<MusicBand> set = database.getMusicBandSet();
            var deleted = set.removeIf(b -> b.owner().equals(ctx.currentUser)
                    && b.name().compareTo(band.name()) < 0);

            return Response.success(deleted);
        }
    }

    @Handler(Command.SHOW)
    private Response<?> commandShow(CommandContext ctx) {
        var bands = database.getMusicBandSet().stream().sorted().collect(Collectors.toList());
        return Response.success(bands);
    }

    @Handler(Command.UPDATE)
    private Response<?> commandUpdate(CommandContext ctx, MusicBand band) throws SQLException {
        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("update music_bands set " +
                    "name = ?, coord_x = ?, coord_y = ?, num_participants = ?, description = ?," +
                    "genre = ?, studio_name = ?, studio_address = ? where owner = ? and id = ?");

            stmt.setString(1, band.name());
            stmt.setDouble(2, band.coordinates().x());
            stmt.setLong(3, band.coordinates().y());
            stmt.setInt(4, band.numberOfParticipants());
            stmt.setString(5, band.description());
            stmt.setString(6, band.genre() == null ? null : band.genre().name());
            stmt.setString(7, band.studio() == null ? null : band.studio().name());
            stmt.setString(8, band.studio() == null ? null : band.studio().address());
            stmt.setString(9, ctx.currentUser);
            stmt.setLong(10, band.id());

            var count = stmt.executeUpdate();
            if (count == 0) return Response.success(false);

            var set = database.getMusicBandSet();
            var oldBand = set.stream().filter(b -> b.id() == band.id()).findFirst().orElseThrow();
            set.remove(oldBand);
            set.add(band.withParams(oldBand.id(), oldBand.owner(), oldBand.creationDate()));

            return Response.success(true);
        }
    }

    @Override
    public Response<?> handle(Request<?> request) {
        LOG.info("Received command: " + request.command());
        if (request.argument() != null)
            LOG.info("Argument: " + request.argument());

        var entry = commands.get(request.command());
        if (entry == null)
            return Response.invalidRequest();

        String currentUser;

        try {
            currentUser = validateAuthToken(request.authToken());
        } catch (SQLException e) {
            return Response.exception(e);
        }

        if (entry.requiresAuth() && currentUser == null)
            return Response.unauthorized();

        var context = new CommandContext(currentUser);

        synchronized (database) {
            return entry.handle(context, request);
        }
    }

    private String validateAuthToken(String token) throws SQLException {
        if (token == null) return null;

        try (var conn = database.getConnection()) {
            var stmt = conn.prepareStatement("select username from auth_tokens where token = ?");
            stmt.setString(1, token);
            var res = stmt.executeQuery();
            if (!res.next()) return null;
            return res.getString(1);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private String generateSalt() {
        return UUID.randomUUID().toString();
    }

    private String hashPassword(String password, String salt) {
        try {
            var digest = MessageDigest.getInstance("SHA-512");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            var hash = digest.digest();

            var sb = new StringBuilder(hash.length * 2);

            for (var b : hash) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<Command, CommandEntry> gatherCommands() {
        var commandMethods = new HashMap<Command, CommandEntry>();

        for (var method : getClass().getDeclaredMethods()) {
            var annotation = method.getAnnotation(Handler.class);
            if (annotation == null) continue;

            var parameterCount = method.getParameterCount();

            if (parameterCount == 0 || parameterCount > 2)
                throw new UnsupportedOperationException("command handlers should receive 1 or 2 arguments");

            var entry = new CommandEntry() {
                @Override
                public boolean requiresAuth() {
                    return annotation.requiresAuth();
                }

                @Override
                public Response<?> handle(CommandContext context, Request<?> request) {
                    try {
                        if (parameterCount == 1) {
                            if (request.argument() != null)
                                return Response.invalidRequest();

                            return (Response<?>) method.invoke(CommandHandler.this, context);
                        } else {
                            var argument = request.argument();
                            if (argument != null) {
                                var parameter = method.getParameters()[1];
                                if (!parameter.getType().isAssignableFrom(argument.getClass())) {
                                    return Response.invalidRequest();
                                }
                            }

                            return (Response<?>) method.invoke(CommandHandler.this, context, argument);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        return Response.exception(e);
                    }
                }
            };

            commandMethods.put(annotation.value(), entry);
        }

        return commandMethods;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Handler {
        Command value();

        boolean requiresAuth() default true;
    }

    private interface CommandEntry {
        boolean requiresAuth();

        Response<?> handle(CommandContext context, Request<?> request);
    }

    private record CommandContext(String currentUser) {
    }
}
