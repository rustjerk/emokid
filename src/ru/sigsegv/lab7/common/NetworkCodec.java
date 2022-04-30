package ru.sigsegv.lab7.common;

import java.io.*;
import java.nio.ByteBuffer;

public class NetworkCodec {
    public static final int MAX_MESSAGE_SIZE = 32 * 1024;

    private static final ByteArrayOutputStream cachedBAOS = new ByteArrayOutputStream(MAX_MESSAGE_SIZE);

    public static ByteBuffer encodeObject(Object object) throws IOException {
        synchronized (cachedBAOS) {
            cachedBAOS.reset();
            var stream = new ObjectOutputStream(cachedBAOS);
            stream.writeObject(object);
            if (cachedBAOS.size() > MAX_MESSAGE_SIZE)
                throw new IOException("message to long");
            return ByteBuffer.wrap(cachedBAOS.toByteArray());
        }
    }

    public static Object decodeObject(ByteBuffer sourceBuffer) throws IOException {
        var offset = sourceBuffer.arrayOffset();
        var length = sourceBuffer.position();
        var rawInput = new ByteArrayInputStream(sourceBuffer.array(), offset, length);
        var input = new ObjectInputStream(rawInput);

        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("unknown class", e);
        }
    }
}
