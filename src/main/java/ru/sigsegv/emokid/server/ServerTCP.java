package ru.sigsegv.emokid.server;

import ru.sigsegv.emokid.common.NetworkCodec;
import ru.sigsegv.emokid.common.Request;
import ru.sigsegv.emokid.common.Response;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerTCP extends Server {
    private static final Logger LOG = Logger.getLogger(ServerTCP.class.getSimpleName());

    private final ServerSocket serverSocket;

    public ServerTCP(ServerContext context, int port) throws IOException {
        super(context);
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000);
    }

    @Override
    public void serve() throws IOException {
        while (context.isRunning.get()) {
            try {
                var clientSocket = serverSocket.accept();
                executeSocketTask(context.readExecutor, clientSocket, this::clientRead);
            } catch (SocketTimeoutException ignored) {
            }
        }
    }

    @FunctionalInterface
    private interface SocketTask {
        void run(Socket socket) throws Exception;
    }

    private void executeSocketTask(ExecutorService executor, Socket socket, SocketTask task) {
        executor.execute(() -> {
            try {
                task.run(socket);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Client " + socket.getRemoteSocketAddress() + " error: " + e.getMessage(), e);

                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void clientRead(Socket socket) throws IOException {
        LOG.info("Reading request from " + socket.getRemoteSocketAddress());

        var offs = 0;
        var buf = new byte[NetworkCodec.MAX_MESSAGE_SIZE];

        Object data;

        while (true) {
            var read = socket.getInputStream().read(buf, offs, buf.length - offs);
            if (read == -1) throw new IOException("connection unexpectedly closed");

            offs += read;

            try {
                var bb = ByteBuffer.wrap(buf, 0, offs);
                bb.position(offs);
                data = NetworkCodec.decodeObject(bb, Request.class);
                break;
            } catch (EOFException ignored) {
            }
        }

        if (!(data instanceof Request<?> request))
            throw new IOException("request expected");

        executeSocketTask(context.handleExecutor, socket, s -> clientHandle(s, request));
    }

    private void clientHandle(Socket socket, Request<?> request) {
        LOG.info("Handling request from " + socket.getRemoteSocketAddress());

        var response = context.requestHandler.handle(request);

        if (response.eventBuffer() != null) {
            executeSocketTask(context.writeExecutor, socket, s -> clientWriteEvents(s, response.eventBuffer()));
        } else {
            executeSocketTask(context.writeExecutor, socket, s -> clientWrite(s, response));
        }
    }

    private void clientWrite(Socket socket, Response<?> response) throws IOException {
        LOG.info("Sending response to " + socket.getRemoteSocketAddress());

        var output = socket.getOutputStream();
        output.write(NetworkCodec.encodeObject(response).array());
        socket.close();
    }

    private void clientWriteEvents(Socket socket, EventBuffer eventBuffer) throws IOException {
        LOG.info("Sending events to " + socket.getRemoteSocketAddress());

        var output = socket.getOutputStream();

        while (true) {
            var events = eventBuffer.receive();
            try {
                output.write(NetworkCodec.encodeObject(events).array());
            } catch (IOException e) {
                break;
            }
        }

        socket.close();
    }
}