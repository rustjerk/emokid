package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.NetworkCodec;
import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerUDP extends Server {
    private static final Logger LOG = Logger.getLogger(ServerUDP.class.getSimpleName());

    private final DatagramSocket socket;

    public ServerUDP(ServerContext context, int port) throws IOException {
        super(context);
        socket = new DatagramSocket(port);
    }

    @Override
    public void serve() throws IOException {
        while (context.isRunning.get()) {
            var buf = new byte[NetworkCodec.MAX_MESSAGE_SIZE];
            var packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            executeTask(context.handleExecutor, packet.getSocketAddress(), () -> handlePacket(packet));
        }
    }

    @FunctionalInterface
    private interface Task {
        void run() throws Exception;
    }

    private void executeTask(ExecutorService executor, SocketAddress address, Task task) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Client " + address + " error: " + e.getMessage(), e);
            }
        });
    }

    private void handlePacket(DatagramPacket packet) throws IOException {
        LOG.info("Received packet from " + packet.getSocketAddress());

        var buf = ByteBuffer.wrap(packet.getData(), packet.getOffset(), packet.getLength());
        buf.position(packet.getLength());
        var data = NetworkCodec.decodeObject(buf);

        if (!(data instanceof Request<?> request))
            throw new IOException("request expected");

        var response = context.requestHandler.handle(request);
        executeTask(context.writeExecutor, packet.getSocketAddress(), () -> writeResponse(packet, response));
    }

    private void writeResponse(DatagramPacket packet, Response<?> response) throws IOException {
        LOG.info("Writing packet to " + packet.getSocketAddress());

        var buf = NetworkCodec.encodeObject(response);
        packet.setData(buf.array(), buf.arrayOffset(), buf.limit());
        socket.send(packet);
    }
}
