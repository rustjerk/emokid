package ru.sigsegv.lab7.server;

import sun.misc.Signal;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("syntax: client <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        File databasePath = getDatabasePath();
        Database database = loadDatabase(databasePath);
        setupSaveHandler(database, databasePath);
        setupShutdownHandler(database, databasePath);

        CommandHandler handler = new CommandHandler(database);
        SocketAddress address = new InetSocketAddress(port);
        Server server = new Server(address, handler);

        while (true) {
            server.tick(100);
        }
    }

    private static File getDatabasePath() {
        String savePath = System.getenv("FILE");
        return new File(savePath == null ? "save.json" : savePath);
    }

    private static Database loadDatabase(File databasePath) {
        Database database = new Database();

        try {
            database.load(databasePath);
        } catch (Exception e) {
            System.err.println("Error loading database: " + e.getMessage());
        }

        return database;
    }

    private static void saveDatabase(Database database, File databasePath) {
        try {
            System.err.println("Saving...");
            database.save(databasePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupSaveHandler(Database database, File databasePath) {
        Signal signal;

        try {
            signal = new Signal("TSTP");
        } catch (Exception e) {
            return;
        }

        Signal.handle(signal, s -> {
            saveDatabase(database, databasePath);
        });
    }

    private static void setupShutdownHandler(Database database, File databasePath) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveDatabase(database, databasePath);
        }));
    }
}