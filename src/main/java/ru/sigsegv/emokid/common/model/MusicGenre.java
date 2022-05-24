package ru.sigsegv.emokid.common.model;

import java.io.Serializable;

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
}
