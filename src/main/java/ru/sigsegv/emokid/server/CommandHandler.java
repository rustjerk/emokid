package ru.sigsegv.emokid.server;

import com.yandex.ydb.core.StatusCode;
import com.yandex.ydb.table.query.Params;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.OptionalValue;
import com.yandex.ydb.table.values.PrimitiveValue;
import ru.sigsegv.emokid.common.Command;
import ru.sigsegv.emokid.common.Request;
import ru.sigsegv.emokid.common.Response;
import ru.sigsegv.emokid.common.model.*;

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
    private final EventBufferSet eventBuffers = new EventBufferSet();

    public CommandHandler(Database database) {
        this.database = database;
    }

    private Response<?> addOrUpdate(CommandContext ctx, MusicBand band, boolean update) {
        var set = database.getMusicBandSet();
        var oldBand = set.stream().filter(b -> b.id() == band.id()).findFirst();
        if (oldBand.isPresent() && !oldBand.get().owner().equals(ctx.currentUser()))
            return Response.unauthorized();

        var query = "DECLARE $id AS Uint64;" +
                "DECLARE $owner AS Utf8;" +
                "DECLARE $name AS Utf8;" +
                "DECLARE $coord_x AS Double;" +
                "DECLARE $coord_y AS Int64;" +
                "DECLARE $creation_date AS Datetime;" +
                "DECLARE $num_participants AS Int32;" +
                "DECLARE $description AS Utf8;" +
                "DECLARE $genre AS Utf8?;" +
                "DECLARE $studio_name AS Utf8?;" +
                "DECLARE $studio_address AS Utf8?;" +
                "UPSERT INTO music_bands (id, owner, name, coord_x, coord_y, creation_date, num_participants, description, genre, studio_name, studio_address)" +
                "VALUES ($id, $owner, $name, $coord_x, $coord_y, $creation_date, $num_participants, $description, $genre, $studio_name, $studio_address)";

        var id = update ? band.id() : database.getNextID();
        var creationDate = update ? band.creationDate() : ZonedDateTime.now();

        var params = Params.create()
                .put("$id", PrimitiveValue.uint64(id))
                .put("$owner", PrimitiveValue.utf8(ctx.currentUser()))
                .put("$name", PrimitiveValue.utf8(band.name()))
                .put("$coord_x", PrimitiveValue.float64(band.coordinates().x()))
                .put("$coord_y", PrimitiveValue.int64(band.coordinates().y()))
                .put("$creation_date", PrimitiveValue.datetime(creationDate.toLocalDateTime()))
                .put("$num_participants", PrimitiveValue.int32(band.numberOfParticipants()))
                .put("$description", PrimitiveValue.utf8(band.description()));

        if (band.genre() != null)
            params = params.put("$genre", OptionalValue.of(PrimitiveValue.utf8(band.genre().name())));

        if (band.studio() != null) {
            if (band.studio().name() != null)
                params = params.put("$studio_name", OptionalValue.of(PrimitiveValue.utf8(band.studio().name())));

            params = params.put("$studio_address", OptionalValue.of(PrimitiveValue.utf8(band.studio().address())));
        }

        var txControl = TxControl.serializableRw().setCommitTx(true);
        var finalParams = params;
        database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, finalParams)).join().expect("failed to execute query");

        var newBand = band.withParams(id, ctx.currentUser(), creationDate);
        oldBand.ifPresent(set::remove);
        set.add(newBand);
        eventBuffers.send(new EventUpdate(newBand));

        return Response.success(id);
    }

    @Handler(Command.ADD)
    private Response<?> commandAdd(CommandContext ctx, MusicBand band) {
        return addOrUpdate(ctx, band, false);
    }

    @Handler(Command.ADD_IF_MAX)
    private Response<?> commandAddIfMax(CommandContext ctx, MusicBand band) {
        var set = database.getMusicBandSet();
        var max = set.stream().max(Comparator.naturalOrder()).orElse(band);
        if (max.compareTo(band) > 0) return Response.success();
        return commandAdd(ctx, band);
    }

    @Handler(Command.CLEAR)
    private Response<?> commandClear(CommandContext ctx) {
        var query = "DECLARE $owner AS Utf8;" +
                "DELETE FROM music_bands WHERE owner = $owner";

        var params = Params.create()
                .put("$owner", PrimitiveValue.utf8(ctx.currentUser()));

        var txControl = TxControl.serializableRw().setCommitTx(true);
        database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params)).join().expect("failed to execute query");

        database.getMusicBandSet().removeIf(b -> {
            var del = b.owner().equals(ctx.currentUser);
            if (del) eventBuffers.send(new EventDelete(b.id()));
            return del;
        });

        return Response.success();
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
    private Response<?> commandLogin(CommandContext ctx, Credentials credentials) {
        var txControl = TxControl.serializableRw().setCommitTx(true);

        String salt;
        {
            var query = "DECLARE $username AS Utf8;" +
                    "SELECT salt FROM users WHERE username = $username";

            var params = Params.create()
                    .put("$username", PrimitiveValue.utf8(credentials.username()));

            var result = database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                    .join().expect("failed to execute query").getResultSet(0);

            if (!result.next()) return Response.noSuchUser();

            salt = result.getColumn("salt").getUtf8();
        }

        var password = hashPassword(credentials.password(), salt);

        {
            var query = "DECLARE $username AS Utf8;" +
                    "DECLARE $password AS Utf8;" +
                    "SELECT * FROM users WHERE username = $username AND password = $password";

            var params = Params.create()
                    .put("$username", PrimitiveValue.utf8(credentials.username()))
                    .put("$password", PrimitiveValue.utf8(password));

            var result = database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                    .join().expect("failed to execute query").getResultSet(0);

            if (!result.next()) return Response.invalidPassword();
        }

        var token = generateToken();

        {
            var query = "DECLARE $username AS Utf8;" +
                    "DECLARE $token AS Utf8;" +
                    "UPSERT INTO auth_tokens (username, token) VALUES ($username, $token)";

            var params = Params.create()
                    .put("$username", PrimitiveValue.utf8(credentials.username()))
                    .put("$token", PrimitiveValue.utf8(token));

            database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                    .join().expect("failed to execute query");
        }

        return Response.success(token);
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
    private Response<?> commandRegister(CommandContext ctx, Credentials credentials) {
        var salt = generateSalt();
        var password = hashPassword(credentials.password(), salt);

        var query = "DECLARE $username AS Utf8;" +
                "DECLARE $password AS Utf8;" +
                "DECLARE $salt AS Utf8;" +
                "INSERT INTO users (username, password, salt) VALUES ($username, $password, $salt)";

        var params = Params.create()
                .put("$username", PrimitiveValue.utf8(credentials.username()))
                .put("$password", PrimitiveValue.utf8(password))
                .put("$salt", PrimitiveValue.utf8(salt));

        var txControl = TxControl.serializableRw().setCommitTx(true);
        var result = database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params)).join();
        if (result.getCode() == StatusCode.PRECONDITION_FAILED) {
            return Response.usernameTaken();
        }

        result.expect("failed to execute query");
        return Response.success();
    }

    @Handler(Command.REMOVE_BY_ID)
    private Response<?> commandRemoveById(CommandContext ctx, Long id) {
        var query = "DECLARE $id AS Uint64;" +
                "DECLARE $owner AS Utf8;" +
                "DELETE FROM music_bands WHERE id = $id AND owner = $owner";

        var params = Params.create()
                .put("$id", PrimitiveValue.uint64(id))
                .put("$owner", PrimitiveValue.utf8(ctx.currentUser));

        var txControl = TxControl.serializableRw().setCommitTx(true);
        database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("failed to execute query");

        var set = database.getMusicBandSet();
        var deleted = set.removeIf(b -> b.id() == id && b.owner().equals(ctx.currentUser));

        eventBuffers.send(new EventDelete(id));

        return Response.success(deleted);
    }

    @Handler(Command.REMOVE_GREATER)
    private Response<?> commandRemoveGreater(CommandContext ctx, MusicBand band) {
        var query = "DECLARE $name AS Utf8;" +
                "DECLARE $owner AS Utf8;" +
                "DELETE FROM music_bands WHERE owner = $owner AND name > $name";

        var params = Params.create()
                .put("$name", PrimitiveValue.utf8(band.name()))
                .put("$owner", PrimitiveValue.utf8(ctx.currentUser));

        var txControl = TxControl.serializableRw().setCommitTx(true);
        database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("failed to execute query");

        var set = database.getMusicBandSet();
        var deleted = set.removeIf(b -> {
            var del = b.owner().equals(ctx.currentUser)
                    && b.name().compareTo(band.name()) > 0;
            if (del) eventBuffers.send(new EventDelete(b.id()));
            return del;
        });

        return Response.success(deleted);
    }

    @Handler(Command.REMOVE_LOWER)
    private Response<?> commandRemoveLower(CommandContext ctx, MusicBand band) {
        var query = "DECLARE $name AS Utf8;" +
                "DECLARE $owner AS Utf8;" +
                "DELETE FROM music_bands WHERE owner = $owner AND name < $name";

        var params = Params.create()
                .put("$name", PrimitiveValue.utf8(band.name()))
                .put("$owner", PrimitiveValue.utf8(ctx.currentUser));

        var txControl = TxControl.serializableRw().setCommitTx(true);
        database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("failed to execute query");

        var set = database.getMusicBandSet();
        var deleted = set.removeIf(b -> {
            var del = b.owner().equals(ctx.currentUser)
                    && b.name().compareTo(band.name()) < 0;
            if (del) eventBuffers.send(new EventDelete(b.id()));
            return del;
        });

        return Response.success(deleted);
    }

    @Handler(Command.SHOW)
    private Response<?> commandShow(CommandContext ctx) {
        var bands = database.getMusicBandSet().stream().sorted().collect(Collectors.toList());
        return Response.success(bands);
    }

    @Handler(Command.SUBSCRIBE)
    private Response<?> commandSubscribe(CommandContext ctx) {
        var eventBuffer = new EventBuffer();
        eventBuffers.add(eventBuffer);
        return Response.subscription(eventBuffer);
    }

    @Handler(Command.UPDATE)
    private Response<?> commandUpdate(CommandContext ctx, MusicBand band) {
        return addOrUpdate(ctx, band, true);
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
            return Response.unauthenticated();

        var context = new CommandContext(currentUser);

        synchronized (database) {
            return entry.handle(context, request);
        }
    }

    private String validateAuthToken(String token) throws SQLException {
        if (token == null) return null;

        var query = "DECLARE $token AS Utf8;" +
                "SELECT username FROM auth_tokens WHERE token = $token";

        var params = Params.create()
                .put("$token", PrimitiveValue.utf8(token));

        var txControl = TxControl.serializableRw().setCommitTx(true);
        var result = database.getConnection().supplyResult(session -> session.executeDataQuery(query, txControl, params))
                .join().expect("failed to execute query").getResultSet(0);
        if (!result.next()) return null;
        return result.getColumn("username").getUtf8();
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
