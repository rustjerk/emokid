package ru.sigsegv.lab7.common;

import java.io.Serializable;

public class Request<T> implements Serializable {
    private final Command command;
    private final T argument;

    public Request(Command command, T argument) {
        this.command = command;
        this.argument = argument;
    }

    public Request(Command command) {
        this.command = command;
        this.argument = null;
    }

    public Command getCommand() {
        return command;
    }

    public T getArgument() {
        return argument;
    }
}
