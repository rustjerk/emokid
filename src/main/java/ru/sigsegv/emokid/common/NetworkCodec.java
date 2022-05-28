package ru.sigsegv.emokid.common;

import ru.sigsegv.emokid.common.serde.DeserializeException;
import ru.sigsegv.emokid.common.serde.SerDe;
import ru.sigsegv.emokid.common.serde.json.JsonDeserializer;
import ru.sigsegv.emokid.common.serde.json.JsonPrettySerializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NetworkCodec {
    public static final int MAX_MESSAGE_SIZE = 32 * 1024;

    public static ByteBuffer encodeObject(Object object) {
        var serializer = new JsonPrettySerializer();
        SerDe.serialize(serializer, object);
        return ByteBuffer.wrap(serializer.getString().getBytes(StandardCharsets.UTF_8));
    }

    public static Object decodeObject(ByteBuffer sourceBuffer, Class<?> type) throws IOException {
        var str = new String(sourceBuffer.array(), 0, sourceBuffer.position(), StandardCharsets.UTF_8);
        var deserializer = new JsonDeserializer(new Scanner(str));
        try {
            return SerDe.deserialize(deserializer, type);
        } catch (DeserializeException e) {
            throw new IOException(e);
        }
    }
}
