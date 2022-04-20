package ru.sigsegv.lab6.client;

import ru.sigsegv.lab6.common.Command;
import ru.sigsegv.lab6.common.NetworkCodec;
import ru.sigsegv.lab6.common.Request;
import ru.sigsegv.lab6.common.Response;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;

public abstract class Client {
    protected final static int TIMEOUT_MS = 2000;

    protected final Selector selector;
    protected final SocketAddress serverAddress;
    protected final ByteBuffer buffer = ByteBuffer.allocate(NetworkCodec.MAX_MESSAGE_SIZE);

    public Client(SocketAddress serverAddress) throws IOException {
        selector = Selector.open();
        this.serverAddress = serverAddress;
    }

    public abstract <T> Response<T> request(Request<?> request) throws IOException;

    public <T> Response<T> request(Command cmd, Object arg) throws IOException {
        return request(new Request<>(cmd, arg));
    }

    public <T> Response<T> request(Command cmd) throws IOException {
        return request(new Request<>(cmd));
    }

    protected void waitUntilSelected(Instant deadline, BooleanSupplier cond) throws IOException {
        while (Instant.now().isBefore(deadline) && !cond.getAsBoolean()) {
            long timeout = Math.max(Duration.between(Instant.now(), deadline).dividedBy(2).toMillis(), 1);
            selector.select(timeout);
        }
    }

    protected <T extends SelectableChannel & ReadableByteChannel & WritableByteChannel, R>
    Response<R> requestByChannel(T channel, Request<?> request) throws IOException {
        Instant deadline = Instant.now().plusMillis(TIMEOUT_MS);

        ByteBuffer requestBuffer = NetworkCodec.encodeObject(request);

        channel.configureBlocking(false);
        SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE);

        while (deadline.isAfter(Instant.now()) && requestBuffer.hasRemaining()) {
            waitUntilSelected(deadline, key::isWritable);
            channel.write(requestBuffer);
        }

        if (requestBuffer.hasRemaining())
            throw new SocketTimeoutException("timed out while sending request");

        buffer.clear();
        key.interestOps(SelectionKey.OP_READ);

        while (deadline.isAfter(Instant.now())) {
            waitUntilSelected(deadline, key::isReadable);
            int numRead = channel.read(buffer);
            if (numRead == 0) continue;
            if (numRead == -1) throw new EOFException("reached EOF while receiving response");

            Object response = NetworkCodec.decodeObject(buffer);
            if (response != null) {
                if (!(response instanceof Response<?>))
                    throw new IOException("invalid response");
                return (Response<R>) response;
            }
        }

        throw new SocketTimeoutException("timed out while receiving response");
    }
}
