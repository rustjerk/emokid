package ru.sigsegv.lab7.telegram;

import ru.sigsegv.lab7.client.CommandHandler;

import java.net.InetSocketAddress;

public class TelegramBridge {
    public static void run(TelegramBot bot, int port) {
        var serverAddress = new InetSocketAddress("localhost", port);
        bot.run(chatId -> new Thread(() -> {
            while (true) {
                try {
                    var context = new TelegramCommandContext(bot, chatId);
                    var handler = new CommandHandler(serverAddress, context);
                    handler.runREPL();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start());
    }
}
