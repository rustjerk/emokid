package ru.sigsegv.emokid.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("syntax: client <hostname> <port>");
            return;
        }

        var hostname = args[0];
        var port = Integer.parseInt(args[1]);
        SocketAddress serverAddress = new InetSocketAddress(hostname, port);

        (new CommandHandler(serverAddress)).runREPL();
    }
}
