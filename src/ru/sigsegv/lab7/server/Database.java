package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.model.Coordinates;
import ru.sigsegv.lab7.common.model.MusicBand;
import ru.sigsegv.lab7.common.model.MusicGenre;
import ru.sigsegv.lab7.common.model.Studio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Database {
    private final String dbURL;
    private final String dbUsername;
    private final String dbPassword;

    private final HashSet<MusicBand> musicBandSet = new LinkedHashSet<>();
    private final LocalDateTime initializationTime = LocalDateTime.now();

    public Database(String dbURL, String dbUsername, String dbPassword) {
        this.dbURL = dbURL;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;

        try {
            loadDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbURL, dbUsername, dbPassword);
    }

    public HashSet<MusicBand> getMusicBandSet() {
        return musicBandSet;
    }

    public LocalDateTime getInitializationTime() {
        return initializationTime;
    }

    public String getType() {
        return "LinkedHashSet";
    }

    private void loadDatabase() throws SQLException {
        try (var conn = getConnection()) {
            var stmt = conn.prepareStatement(
                    "select id, owner, name, coord_x, coord_y, creation_date, " +
                            "num_participants, description, genre, studio_name, studio_address from music_bands");
            var res = stmt.executeQuery();

            while (res.next()) {
                var id = res.getLong(1);
                var owner = res.getString(2);
                var name = res.getString(3);
                var coordX = res.getDouble(4);
                var coordY = res.getLong(5);
                var creationDate = res.getObject(6, OffsetDateTime.class).toZonedDateTime();
                var numParticipants = res.getInt(7);
                var description = res.getString(8);
                var genreStr = res.getString(9);
                var studioName = res.getString(10);
                var studioAddress = res.getString(11);

                var coordinates = new Coordinates(coordX, coordY);
                var genre = Arrays.stream(MusicGenre.values()).filter(v -> v.name().equals(genreStr))
                        .findFirst().orElse(null);
                var studio = studioAddress == null ? null : new Studio(studioName, studioAddress);

                var musicBand = new MusicBand(id, owner, name, coordinates, creationDate, numParticipants, description, genre, studio);
                musicBandSet.add(musicBand);
            }
        }
    }
}
