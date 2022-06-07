package ru.sigsegv.emokid.server;

public record Config(int port, String dbURL, String telegramToken) {
}
