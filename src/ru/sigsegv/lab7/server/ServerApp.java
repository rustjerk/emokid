package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.serde.DeserializeException;
import ru.sigsegv.lab7.common.serde.SerDe;
import ru.sigsegv.lab7.common.serde.json.JsonDeserializer;
import ru.sigsegv.lab7.telegram.TelegramBot;
import ru.sigsegv.lab7.telegram.TelegramBridge;

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

        var serverThreads = new Thread[]{
                spawnServer(new ServerTCP(context, config.port())),
                spawnServer(new ServerUDP(context, config.port()))
        };

        TelegramBot telegramBot = null;
        Thread telegramThread = null;

        for (var thread : serverThreads)
            thread.start();

        loop:
        while (true) {
            var line = new Scanner(System.in).nextLine();
            if (line == null) break;

            switch (line) {
                case "exit" -> {
                    break loop;
                }

                case "tg start" -> {
                    if (telegramBot != null) {
                        System.out.println("Telegram bot already running.");
                        break;
                    }

                    telegramBot = new TelegramBot(config.telegramToken());

                    var finalTgBot = telegramBot;
                    telegramThread = new Thread(() -> TelegramBridge.run(finalTgBot, config.port()));
                    telegramThread.start();
                }

                case "tg stop" -> {
                    if (telegramBot == null) {
                        System.out.println("Telegram bot already stopped.");
                        break;
                    }

                    telegramBot.stop();
                    telegramBot = null;
                    telegramThread = null;
                }

                case "help" -> System.out.println("Available commands: exit, tg start, tg stop");

                default -> {
                }
            }
        }

        System.out.println("Exiting gracefully...");

        context.stop();
        if (telegramBot != null) telegramBot.stop();

        joinThreads(serverThreads);
        if (telegramThread != null) joinThreads(telegramThread);
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

    private static void joinThreads(Thread... threads) {
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
}
