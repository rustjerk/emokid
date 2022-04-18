package ru.sigsegv.lab6.client;

import ru.sigsegv.lab6.common.Request;
import ru.sigsegv.lab6.common.Response;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class ClientUDP extends Client {
    public ClientUDP(SocketAddress serverAddress) throws IOException {
        super(serverAddress);
    }

    @Override
    public <T> Response<T> request(Request<?> request) throws IOException {
        DatagramChannel socket = DatagramChannel.open().connect(serverAddress);
        return requestByChannel(socket, request);
    }
}
