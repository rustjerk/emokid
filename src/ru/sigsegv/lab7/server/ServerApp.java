package ru.sigsegv.lab7.server;

import java.io.File;
import java.io.IOException;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("syntax: client <port>");
            return;
        }

        var port = Integer.parseInt(args[0]);

        var databasePath = getDatabasePath();
        var database = loadDatabase(databasePath);
        setupShutdownHandler(database, databasePath);

        var handler = new CommandHandler(database);
        var context = new ServerContext(handler);

        var threads = new Thread[]{
                spawnServer(new ServerTCP(context, port)),
                spawnServer(new ServerUDP(context, port))
        };

        for (var thread : threads)
            thread.start();

        for (var thread : threads) {
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private static Thread spawnServer(Server server) {
        return new Thread(() -> {
            try {
                server.serve();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static File getDatabasePath() {
        var savePath = System.getenv("FILE");
        return new File(savePath == null ? "save.json" : savePath);
    }

    private static Database loadDatabase(File databasePath) {
        var database = new Database();

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

    private static void setupShutdownHandler(Database database, File databasePath) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveDatabase(database, databasePath)));
    }
}
