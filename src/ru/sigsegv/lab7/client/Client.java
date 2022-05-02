package ru.sigsegv.lab7.client;

import ru.sigsegv.lab7.common.Command;
import ru.sigsegv.lab7.common.NetworkCodec;
import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;

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
    protected final static int TIMEOUT_MS = 10000;

    protected Selector selector;
    protected final SocketAddress serverAddress;
    protected final ByteBuffer buffer = ByteBuffer.allocate(NetworkCodec.MAX_MESSAGE_SIZE);

    protected String authToken;

    public Client(SocketAddress serverAddress) throws IOException {
        selector = Selector.open();
        this.serverAddress = serverAddress;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isAuthenticated() {
        return authToken != null;
    }

    public <T> Response<T> request(Request<?> request) throws IOException {
        return requestImpl(new Request<>(request.command(), request.argument(), authToken));
    }

    public <T> Response<T> request(Command cmd, Object arg) throws IOException {
        return request(new Request<>(cmd, arg));
    }

    public <T> Response<T> request(Command cmd) throws IOException {
        return request(new Request<>(cmd));
    }

    protected abstract <T> Response<T> requestImpl(Request<?> request) throws IOException;

    protected void waitUntilSelected(Instant deadline, BooleanSupplier cond) throws IOException {
        while (Instant.now().isBefore(deadline) && !cond.getAsBoolean()) {
            var timeout = Math.max(Duration.between(Instant.now(), deadline).dividedBy(2).toMillis(), 1);
            selector.select(timeout);
        }
    }

    private static final boolean IS_NIO_BROKEN = System.getProperty("os.name").toLowerCase().contains("win");

    @SuppressWarnings("unchecked")
    protected <T extends SelectableChannel & ReadableByteChannel & WritableByteChannel, R>
    Response<R> requestByChannel(T channel, Request<?> request) throws IOException {
        channel.configureBlocking(IS_NIO_BROKEN);

        var deadline = Instant.now().plusMillis(TIMEOUT_MS);
        var requestBuffer = NetworkCodec.encodeObject(request);

        SelectionKey key = null;
        if (!IS_NIO_BROKEN) key = channel.register(selector, SelectionKey.OP_WRITE);

        while (deadline.isAfter(Instant.now()) && requestBuffer.hasRemaining()) {
            if (!IS_NIO_BROKEN) waitUntilSelected(deadline, key::isWritable);
            channel.write(requestBuffer);
        }

        if (requestBuffer.hasRemaining())
            throw new SocketTimeoutException("timed out while sending request");

        if (!IS_NIO_BROKEN) key.interestOps(SelectionKey.OP_READ);

        buffer.clear();

        while (deadline.isAfter(Instant.now())) {
            if (!IS_NIO_BROKEN) waitUntilSelected(deadline, key::isReadable);
            var numRead = channel.read(buffer);
            if (numRead == 0) continue;
            if (numRead == -1) throw new EOFException("reached EOF while receiving response");

            var response = NetworkCodec.decodeObject(buffer);
            if (response != null) {
                if (!(response instanceof Response<?>))
                    throw new IOException("invalid response");
                return (Response<R>) response;
            }
        }

        throw new SocketTimeoutException("timed out while receiving response");
    }
}
