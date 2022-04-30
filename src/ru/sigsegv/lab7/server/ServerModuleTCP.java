package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.NetworkCodec;
import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.logging.Logger;

public class ServerModuleTCP implements ServerModule {
    private static final Logger logger = Logger.getLogger(ServerModuleTCP.class.getSimpleName());

    private static final int INACTIVITY_TIMEOUT_MS = 3000;

    private final ServerSocketChannel serverSocket;
    private final RequestHandler requestHandler;

    public ServerModuleTCP(SocketAddress address, RequestHandler requestHandler, Selector selector) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(address);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        this.requestHandler = requestHandler;
    }

    @Override
    public void update(Selector selector) throws IOException {
        var currentTime = Instant.now();

        for (var key : selector.keys()) {
            if (key.attachment() instanceof Client client) {
                var socket = (SocketChannel) key.channel();
                if (!socket.isOpen()) continue;

                var deadline = client.lastActivity.plusMillis(INACTIVITY_TIMEOUT_MS);
                if (currentTime.isAfter(deadline)) {
                    socket.close();
                    logger.info("Closed connection with " + client.address + " due to inactivity");
                }
            }
        }
    }

    @Override
    public void handleSelectedKey(SelectionKey key) throws IOException {
        if (key.channel() == serverSocket)
            handleServerKey(key);
        else
            handleClientKey(key);
    }

    private void handleServerKey(SelectionKey key) throws IOException {
        if (!key.isAcceptable()) return;

        var clientSocket = serverSocket.accept();
        if (clientSocket == null) return;

        var client = new Client();
        client.address = clientSocket.getRemoteAddress();
        client.lastActivity = Instant.now();
        client.buffer = ByteBuffer.allocate(NetworkCodec.MAX_MESSAGE_SIZE);

        clientSocket.configureBlocking(false);
        clientSocket.register(key.selector(), SelectionKey.OP_READ, client);

        logger.info("Accepted client " + client.address);
    }

    private void handleClientKey(SelectionKey key) throws IOException {
        if (!(key.attachment() instanceof Client client)) return;

        var clientSocket = (SocketChannel) key.channel();

        try {
            if (key.isReadable())
                clientRead(client, key, clientSocket);
            else if (key.isWritable())
                clientWrite(client, clientSocket);
        } catch (Exception e) {
            logger.warning("Error while handling client " + client.address);
            e.printStackTrace();

            clientSocket.close();
        }
    }

    private void clientRead(Client client, SelectionKey key, SocketChannel clientSocket) throws IOException {
        var bytesRead = clientSocket.read(client.buffer);
        if (bytesRead == 0) return;

        if (bytesRead == -1) {
            logger.warning("Client " + client.address + " unexpectedly closed TCP stream");
            clientSocket.close();
            return;
        }

        client.lastActivity = Instant.now();

        Object request;
        try {
            request = NetworkCodec.decodeObject(client.buffer);
        } catch (EOFException e) {
            return;
        }

        if (request instanceof Request<?>) {
            handleRequest(client, key, (Request<?>) request);
        } else {
            sendResponse(client, key, Response.invalidRequest());
        }
    }

    private void handleRequest(Client client, SelectionKey key, Request<?> request) throws IOException {
        logger.info("Got request from " + client.address);

        var response = requestHandler.handle(request);
        sendResponse(client, key, response);
    }

    private void sendResponse(Client client, SelectionKey key, Response<?> response) throws IOException {
        client.buffer = NetworkCodec.encodeObject(response);
        key.interestOps(SelectionKey.OP_WRITE);
        clientWrite(client, (SocketChannel) key.channel());
    }

    private void clientWrite(Client client, SocketChannel clientSocket) throws IOException {
        var bytesWrote = clientSocket.write(client.buffer);
        if (bytesWrote == 0) return;

        if (!client.buffer.hasRemaining()) {
            clientSocket.close();
            logger.info("Sent response to " + client.address);
        }
    }

    private static class Client {
        SocketAddress address;
        Instant lastActivity;
        ByteBuffer buffer;
    }
}
