package ru.sigsegv.emokid.client;

import ru.sigsegv.emokid.common.Request;
import ru.sigsegv.emokid.common.Response;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class ClientUDP extends Client {
    public ClientUDP(SocketAddress serverAddress) throws IOException {
        super(serverAddress);
    }

    @Override
    public <T> Response<T> requestImpl(Request<?> request) throws IOException {
        var socket = DatagramChannel.open().connect(serverAddress);
        return requestByChannel(socket, request);
    }
}
