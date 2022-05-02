package ru.sigsegv.lab7.server;

public record Config(int port, String dbURL, String dbUser, String dbPass, String telegramToken) {
}
