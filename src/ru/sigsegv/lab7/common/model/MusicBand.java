package ru.sigsegv.lab7.common.model;

import ru.sigsegv.lab7.client.CommandDeserializer;
import ru.sigsegv.lab7.common.serde.SkipDeserialization;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Music band data record
 */
public record MusicBand(@SkipDeserialization(deserializer = CommandDeserializer.class)
                        long id,
                        String name,
                        Coordinates coordinates,
                        @SkipDeserialization(deserializer = CommandDeserializer.class)
                        ZonedDateTime creationDate,
                        int numberOfParticipants,
                        String description,
                        MusicGenre genre,
                        Studio studio) implements Comparable<MusicBand>, Serializable {

    public MusicBand withId(long id) {
        return new MusicBand(id, name, coordinates, creationDate, numberOfParticipants, description, genre, studio);
    }

    @Override
    public int compareTo(MusicBand o) {
        return name.compareTo(o.name);
    }
}
