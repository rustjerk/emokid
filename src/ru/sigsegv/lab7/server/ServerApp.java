package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.serde.DeserializeException;
import ru.sigsegv.lab7.common.serde.SerDe;
import ru.sigsegv.lab7.common.serde.json.JsonDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("syntax: client <cfg_file>");
            return;
        }

        var config = readConfig(args[0]);

        var database = new Database(config.dbURL(), config.dbUser(), config.dbPass());
        var handler = new CommandHandler(database);
        var context = new ServerContext(handler);

        var threads = new Thread[]{
                spawnServer(new ServerTCP(context, config.port())),
                spawnServer(new ServerUDP(context, config.port()))
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

    private static Config readConfig(String path) {
        try {
            var deserializer = new JsonDeserializer(new Scanner(new File(path)));
            return SerDe.deserialize(deserializer, Config.class);
        } catch (IOException | DeserializeException e) {
            throw new RuntimeException(e);
        }
    }
}
