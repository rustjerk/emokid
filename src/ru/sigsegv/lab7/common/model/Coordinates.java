package ru.sigsegv.lab7.common.model;

import java.io.Serializable;

public record Coordinates(double x, long y) implements Serializable {
    public Coordinates {
        if (x > 41)
            throw new IllegalArgumentException("x cannot be above 41");
        if (y > 107)
            throw new IllegalArgumentException("y cannot be above 107");
    }
}