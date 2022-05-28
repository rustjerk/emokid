package ru.sigsegv.emokid.common;

import ru.sigsegv.emokid.common.serde.Nullable;

public record Request<T>(Command command, @Nullable T argument, @Nullable String authToken) {
    public Request(Command command, T argument) {
        this(command, argument, null);
    }

    public Request(Command command) {
        this(command, null, null);
    }
}
