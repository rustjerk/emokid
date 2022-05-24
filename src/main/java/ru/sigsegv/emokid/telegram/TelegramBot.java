package ru.sigsegv.emokid.telegram;

import ru.sigsegv.emokid.common.serde.json.dom.JsonMap;
import ru.sigsegv.emokid.common.serde.json.dom.JsonValue;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TelegramBot {
    private final String apiToken;
    private final Map<Integer, BlockingQueue<String>> messageQueues = new ConcurrentHashMap<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    public TelegramBot(String apiToken) {
        this.apiToken = apiToken;
    }

    public void run(Consumer<Integer> newUserConsumer) {
        var lastUpdate = 0;

        isRunning.set(true);

        while (isRunning.get()) {
            var req = new JsonMap().put("timeout", 1).put("offset", lastUpdate + 1);
            var res = apiRequest("getUpdates", req).asList();
            for (var updateRaw : res.inner) {
                var update = updateRaw.asMap();
                lastUpdate = update.get("update_id").asInt();

                if (!update.containsKey("message")) continue;

                var message = update.get("message").asMap();
                if (!message.containsKey("text")) continue;

                var chatId = message.get("from").asMap().get("id").asInt();
                var text = message.get("text").asString();

                if (!messageQueues.containsKey(chatId)) {
                    newUserConsumer.accept(chatId);
                    messageQueues.put(chatId, new ArrayBlockingQueue<>(256));
                }

                var queue = messageQueues.get(chatId);
                while (true) {
                    try {
                        queue.put(text);
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void stop() {
        isRunning.set(false);
    }

    public String receive(int chatId) {
        var queue = messageQueues.computeIfAbsent(chatId, v -> new ArrayBlockingQueue<>(256));
        while (true) {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(int chatId, String text) {
        var req = new JsonMap()
                .put("chat_id", chatId)
                .put("text", text)
                .put("parse_mode", "HTML");
        apiRequest("sendMessage", req);
    }

    public JsonValue apiRequest(String method, JsonValue request) {
        // unfortunately HttpClient doesn't work on helios

        try {
            var json = request.toString();

            var socket = SSLSocketFactory.getDefault().createSocket("api.telegram.org", 443);
            var output = new OutputStreamWriter(new BufferedOutputStream(socket.getOutputStream()));

            output.write("GET /bot" + apiToken + "/" + method + " HTTP/1.1\r\n");
            output.write("Host: api.telegram.org\r\n");
            output.write("Connection: close\r\n");
            output.write("Content-Type: application/json\r\n");
            output.write("Content-Length: " + json.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n");
            output.write(json);
            output.flush();

            var input = socket.getInputStream();
            var lineInput = new BufferedReader(new InputStreamReader(input));
            var contentLength = 0;
            while (true) {
                var line = lineInput.readLine();
                if (line.toLowerCase().startsWith("content-length: ")) {
                    contentLength = Integer.parseInt(line.substring(16));
                }

                if (line.isEmpty()) break;
            }

            var buf = new char[contentLength];
            var read = lineInput.read(buf);
            var body = new String(buf, 0, read);

            var jsonRes = JsonValue.fromString(body).asMap();
            if (!jsonRes.get("ok").asBoolean())
                throw new RuntimeException("telegram error: " + jsonRes.get("description").asString());
            return jsonRes.get("result");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
