package ru.sigsegv.lab7.common.model;

import ru.sigsegv.lab7.client.CommandDeserializer;
import ru.sigsegv.lab7.common.serde.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Music band data record
 */
public record MusicBand(@SkipDeserialization(deserializer = CommandDeserializer.class)
                        long id,
                        @SkipDeserialization(deserializer = CommandDeserializer.class)
                        String owner,
                        @Validate(NonBlankValidator.class)
                        String name,
                        Coordinates coordinates,
                        @SkipDeserialization(deserializer = CommandDeserializer.class)
                        ZonedDateTime creationDate,
                        @Validate(NumberOfParticipantsValidator.class)
                        int numberOfParticipants,
                        @Validate(NonBlankValidator.class)
                        String description,
                        @Nullable
                        MusicGenre genre,
                        @Nullable
                        Studio studio) implements Comparable<MusicBand>, Serializable {

    public MusicBand withId(long id) {
        return new MusicBand(id, owner, name, coordinates, creationDate, numberOfParticipants, description, genre, studio);
    }

    public MusicBand withParams(long id, String owner, ZonedDateTime creationDate) {
        return new MusicBand(id, owner, name, coordinates, creationDate, numberOfParticipants, description, genre, studio);
    }

    @Override
    public int compareTo(MusicBand o) {
        return name.compareTo(o.name);
    }

    public static class NumberOfParticipantsValidator implements Validator<Integer> {
        @Override
        public void validate(Integer numberOfParticipants) throws ValidationException {
            if (numberOfParticipants <= 0) throw new ValidationException("should be above 0");
        }
    }
}
