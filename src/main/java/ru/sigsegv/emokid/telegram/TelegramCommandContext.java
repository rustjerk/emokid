package ru.sigsegv.emokid.telegram;

import ru.sigsegv.emokid.client.CommandContext;

import java.util.stream.Collectors;

public class TelegramCommandContext extends CommandContext {
    private final TelegramBot bot;
    private final int chatId;

    private final StringBuilder stringBuilder = new StringBuilder();

    public TelegramCommandContext(TelegramBot bot, int chatId) {
        this.bot = bot;
        this.chatId = chatId;
    }

    @Override
    public String defaultPrompt() {
        return "";
    }

    @Override
    public String readLine(String prompt) {
        flush();
        if (!prompt.isEmpty())
            bot.send(chatId, "<pre>" + escapeHTML(prompt) + "</pre>");
        return bot.receive(chatId);
    }

    @Override
    public void print(String line) {
        stringBuilder.append(line);
    }

    @Override
    public void println(String line) {
        stringBuilder.append(line);
        stringBuilder.append("\n");
    }

    @Override
    public void flush() {
        var lines = new StringBuilder();

        while (true) {
            var newline = stringBuilder.indexOf("\n");
            if (newline == -1) break;

            var line = stringBuilder.substring(0, newline + 1);
            lines.append(line);
            stringBuilder.delete(0, newline + 1);
        }

        if (!lines.isEmpty())
            bot.send(chatId, "<pre>" + escapeHTML(lines.toString()) + "</pre>");
    }

    private static String escapeHTML(String str) {
        return str.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                        "&#" + c + ";" : new String(Character.toChars(c)))
                .collect(Collectors.joining());
    }
}
