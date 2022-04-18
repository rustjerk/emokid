package ru.sigsegv.lab6.server;

import ru.sigsegv.lab6.common.model.MusicBand;
import ru.sigsegv.lab6.common.serde.DeserializeException;
import ru.sigsegv.lab6.common.serde.Deserializer;
import ru.sigsegv.lab6.common.serde.Serializer;
import ru.sigsegv.lab6.common.serde.json.JsonDeserializer;
import ru.sigsegv.lab6.common.serde.json.JsonPrettySerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Database containing music bands
 */
public class Database {
    private final HashSet<MusicBand> musicBandSet = new LinkedHashSet<>();
    private final LocalDateTime initializationTime = LocalDateTime.now();

    /**
     * Gets set of the music bands
     * @return set of the music bands
     */
    public HashSet<MusicBand> getMusicBandSet() {
        return musicBandSet;
    }

    /**
     * Gets initialization time
     * @return initialization time
     */
    public LocalDateTime getInitializationTime() {
        return initializationTime;
    }

    /**
     * Gets type of the collection
     * @return type of the collection
     */
    public String getType() {
        return "LinkedHashSet";
    }

    /**
     * Saves database to a file
     * @param file file to save into
     * @throws IOException if there is an error writing to file
     */
    public void save(File file) throws IOException {
        JsonPrettySerializer serializer = new JsonPrettySerializer();
        Serializer.Seq map = serializer.serializeSeq();

        for (MusicBand band : musicBandSet) {
            map.serializeValue(band);
        }

        map.finish();
        serializer.getString();

        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        stream.write(serializer.getString().getBytes());
        stream.flush();
        stream.close();
    }

    /**
     * Loads database from a file
     * @param file file to load from
     * @throws IOException if there is an error reading from file
     * @throws DeserializeException if the input has errors
     */
    public void load(File file) throws IOException, DeserializeException {
        Deserializer deserializer = new JsonDeserializer(new Scanner(file));
        Deserializer.Seq map = deserializer.deserializeSeq();

        Set<Long> ids = new HashSet<>();

        musicBandSet.clear();
        while (map.hasNext()) {
            MusicBand band = new MusicBand();
            map.nextValue(band);
            if (ids.contains(band.getId()))
                throw new DeserializeException(deserializer.formatErrorMessage("ID collision: %d", band.getId()));
            ids.add(band.getId());
            musicBandSet.add(band);
        }

        map.finish();
    }
}
