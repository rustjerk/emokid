package ru.sigsegv.lab6.client;

import ru.sigsegv.lab6.common.Request;
import ru.sigsegv.lab6.common.Response;

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
