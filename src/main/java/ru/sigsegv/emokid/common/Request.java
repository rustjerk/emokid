package ru.sigsegv.emokid.common;

import java.io.Serializable;

public record Request<T>(Command command, T argument, String authToken) implements Serializable {
    public Request(Command command, T argument) {
        this(command, argument, null);
    }

    public Request(Command command) {
        this(command, null, null);
    }
}
