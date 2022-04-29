package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.NetworkCodec;
import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Logger;

public class ServerModuleUDP implements ServerModule {
    private static final Logger logger = Logger.getLogger(ServerModuleTCP.class.getSimpleName());

    private final DatagramChannel channel;
    private final SelectionKey selectionKey;
    private final RequestHandler requestHandler;

    private final ByteBuffer buffer = ByteBuffer.allocate(NetworkCodec.MAX_MESSAGE_SIZE);
    private final Queue<SocketAddress> writeQueueAddresses = new ArrayDeque<>();
    private final Queue<ByteBuffer> writeQueueBuffers = new ArrayDeque<>();

    public ServerModuleUDP(SocketAddress address, RequestHandler requestHandler, Selector selector) throws IOException {
        channel = DatagramChannel.open();
        channel.bind(address);
        channel.configureBlocking(false);
        selectionKey = channel.register(selector, SelectionKey.OP_READ);

        this.requestHandler = requestHandler;
    }

    @Override
    public void update(Selector selector) throws IOException {
    }

    @Override
    public void handleSelectedKey(SelectionKey key) throws IOException {
        if (key != selectionKey) return;

        if (key.isReadable())
            read();
        if (key.isWritable())
            write();
    }

    private void read() throws IOException {
        buffer.clear();
        SocketAddress clientAddress = channel.receive(buffer);
        if (clientAddress == null) return;

        try {
            Object request = NetworkCodec.decodeObject(buffer);
            if (request instanceof Request<?>) {
                handleRequest(clientAddress, (Request<?>) request);
            } else {
                sendResponse(clientAddress, Response.invalidRequest());
            }
        } catch (Exception e) {
            logger.warning("Error while handling client " + clientAddress);
            e.printStackTrace();
        }
    }

    private void handleRequest(SocketAddress clientAddress, Request<?> request) throws IOException {
        logger.info(request.toString());
        logger.info("Got request from " + clientAddress);

        Response<?> response = requestHandler.handle(request);
        sendResponse(clientAddress, response);
    }

    private void sendResponse(SocketAddress clientAddress, Response<?> response) throws IOException {
        ByteBuffer responseBuffer = NetworkCodec.encodeObject(response);

        writeQueueAddresses.add(clientAddress);
        writeQueueBuffers.add(responseBuffer);

        if (writeQueueBuffers.size() > 0) {
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    private void write() throws IOException {
        while (!writeQueueAddresses.isEmpty()) {
            SocketAddress clientAddress = writeQueueAddresses.peek();
            ByteBuffer responseBuffer = writeQueueBuffers.peek();

            int numSent = channel.send(responseBuffer, clientAddress);
            if (numSent == 0) return;

            logger.info("Sent response to " + clientAddress);

            writeQueueAddresses.remove();
            writeQueueBuffers.remove();
        }

        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
