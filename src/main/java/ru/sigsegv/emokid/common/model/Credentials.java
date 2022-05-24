package ru.sigsegv.emokid.common.model;

import java.io.Serializable;

public record Credentials(String username, String password) implements Serializable {
    @Override
    public String toString() {
        return "Credentials{" +
                "username='" + username + '\'' +
                '}';
    }
}
