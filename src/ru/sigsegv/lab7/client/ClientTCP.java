package ru.sigsegv.lab7.client;

import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class ClientTCP extends Client {
    public ClientTCP(SocketAddress serverAddress) throws IOException {
        super(serverAddress);
    }

    @Override
    public <T> Response<T> request(Request<?> request) throws IOException {
        SocketChannel socket = SocketChannel.open(serverAddress);
        return requestByChannel(socket, request);
    }
}
