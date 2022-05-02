package ru.sigsegv.lab7.common.model;

import java.io.Serializable;

public record Credentials(String username, String password) implements Serializable {
    @Override
    public String toString() {
        return "Credentials{" +
                "username='" + username + '\'' +
                '}';
    }
}
