package ru.sigsegv.emokid.server;

import com.yandex.ydb.auth.iam.CloudAuthProvider;
import com.yandex.ydb.core.grpc.GrpcTransport;
import com.yandex.ydb.table.SessionRetryContext;
import com.yandex.ydb.table.TableClient;
import com.yandex.ydb.table.description.TableDescription;
import com.yandex.ydb.table.rpc.grpc.GrpcTableRpc;
import com.yandex.ydb.table.transaction.TxControl;
import com.yandex.ydb.table.values.PrimitiveType;
import ru.sigsegv.emokid.common.model.Coordinates;
import ru.sigsegv.emokid.common.model.MusicBand;
import ru.sigsegv.emokid.common.model.MusicGenre;
import ru.sigsegv.emokid.common.model.Studio;
import yandex.cloud.sdk.auth.provider.ApiKeyCredentialProvider;

import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Database {
    private final HashSet<MusicBand> musicBandSet = new LinkedHashSet<>();
    private final ZonedDateTime initializationTime = ZonedDateTime.now();

    private final SessionRetryContext retryCtx;
    private final String database;

    private long nextID = 0L;

    public Database(String dbURL) {
        var transport = GrpcTransport.forConnectionString(dbURL)
                .withAuthProvider(CloudAuthProvider.newAuthProvider(ApiKeyCredentialProvider.builder()
                        .fromFile(Path.of("yandex-key.json"))
                        .build()))
                .build();

        var rpc = GrpcTableRpc.ownTransport(transport);
        var tableClient = TableClient.newClient(rpc).build();
        retryCtx = SessionRetryContext.create(tableClient).build();

        this.database = transport.getDatabase();

        createTables();
        loadDatabase();
    }

    public SessionRetryContext getConnection() {
        return retryCtx;
    }

    public HashSet<MusicBand> getMusicBandSet() {
        return musicBandSet;
    }

    public ZonedDateTime getInitializationTime() {
        return initializationTime;
    }

    public String getType() {
        return "LinkedHashSet";
    }

    public long getNextID() {
        return nextID++;
    }

    private void createTables() {
        var usersTable = TableDescription.newBuilder()
                .addNullableColumn("username", PrimitiveType.utf8())
                .addNullableColumn("password", PrimitiveType.utf8())
                .addNullableColumn("salt", PrimitiveType.utf8())
                .setPrimaryKey("username")
                .build();

        retryCtx.supplyStatus(session -> session.createTable(database + "/users", usersTable))
                .join().expect("failed to create table");

        var authTokensTable = TableDescription.newBuilder()
                .addNullableColumn("username", PrimitiveType.utf8())
                .addNullableColumn("token", PrimitiveType.utf8())
                .setPrimaryKey("username")
                .build();

        retryCtx.supplyStatus(session -> session.createTable(database + "/auth_tokens", authTokensTable))
                .join().expect("failed to create table");

        var musicBandsTable = TableDescription.newBuilder()
                .addNullableColumn("id", PrimitiveType.uint64())
                .addNullableColumn("owner", PrimitiveType.utf8())
                .addNullableColumn("name", PrimitiveType.utf8())
                .addNullableColumn("coord_x", PrimitiveType.float64())
                .addNullableColumn("coord_y", PrimitiveType.int64())
                .addNullableColumn("creation_date", PrimitiveType.datetime())
                .addNullableColumn("num_participants", PrimitiveType.int32())
                .addNullableColumn("description", PrimitiveType.utf8())
                .addNullableColumn("genre", PrimitiveType.utf8())
                .addNullableColumn("studio_name", PrimitiveType.utf8())
                .addNullableColumn("studio_address", PrimitiveType.utf8())
                .setPrimaryKey("id")
                .build();

        retryCtx.supplyStatus(session -> session.createTable(database + "/music_bands", musicBandsTable))
                .join().expect("failed to create table");
    }

    private void loadDatabase() {
        var txControl = TxControl.serializableRw().setCommitTx(true);

        var query = "select * from music_bands";

        var result = retryCtx.supplyResult(session -> session.executeDataQuery(query, txControl))
                .join().expect("failed to query data").getResultSet(0);

        while (result.next()) {
            var id = result.getColumn("id").getUint64();
            var owner = result.getColumn("owner").getUtf8();
            var name = result.getColumn("name").getUtf8();
            var coordX = result.getColumn("coord_x").getFloat64();
            var coordY = result.getColumn("coord_y").getInt64();
            var creationDate = result.getColumn("creation_date").getDatetime()
                    .atZone(ZoneId.systemDefault());
            var numParticipants = result.getColumn("num_participants").getInt32();
            var description = result.getColumn("description").getUtf8();
            var genreStrOpt = result.getColumn("genre").getOptionalItem();
            var genreStr = genreStrOpt.isOptionalItemPresent() ? genreStrOpt.getUtf8() : null;
            var studioNameOpt = result.getColumn("studio_name").getOptionalItem();
            var studioName = studioNameOpt.isOptionalItemPresent() ? studioNameOpt.getUtf8() : null;
            var studioAddressOpt = result.getColumn("studio_address").getOptionalItem();
            var studioAddress = studioAddressOpt.isOptionalItemPresent() ? studioAddressOpt.getUtf8() : null;

            var coordinates = new Coordinates(coordX, coordY);
            var genre = Arrays.stream(MusicGenre.values()).filter(v -> v.name().equals(genreStr))
                    .findFirst().orElse(null);
            var studio = studioAddress == null ? null : new Studio(studioName, studioAddress);

            var musicBand = new MusicBand(id, owner, name, coordinates, creationDate, numParticipants, description, genre, studio);
            musicBandSet.add(musicBand);
        }

        nextID = musicBandSet.stream().map(MusicBand::id).max(Comparator.naturalOrder()).orElse(0L) + 1;
    }
}
