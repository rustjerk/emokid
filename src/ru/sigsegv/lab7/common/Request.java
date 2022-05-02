package ru.sigsegv.lab7.common;

import java.io.Serializable;

public record Request<T>(Command command, T argument, String authToken) implements Serializable {
    public Request(Command command, T argument) {
        this(command, argument, null);
    }

    public Request(Command command) {
        this(command, null, null);
    }
}
