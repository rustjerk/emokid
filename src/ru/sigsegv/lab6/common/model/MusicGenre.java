package ru.sigsegv.lab6.common.model;

import ru.sigsegv.lab6.common.serde.Serializable;
import ru.sigsegv.lab6.common.serde.Serializer;

/**
 * Enumeration of known music genres
 */
public enum MusicGenre implements Serializable {
    HIP_HOP("Hip Hop"),
    POST_ROCK("Post Rock"),
    POST_PUNK("Cute Rock");

    private final String name;

    MusicGenre(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Tries to interpret a string as a music genre
     *
     * @param str input string
     * @return corresponding enum constant, or null if there is no such music genre
     */
    public static MusicGenre fromString(String str) {
        for (MusicGenre genre : values())
            if (genre.name.equalsIgnoreCase(str))
                return genre;
        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.serializeString(toString());
    }
}
