package ru.sigsegv.lab7.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("syntax: client <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        SocketAddress serverAddress = new InetSocketAddress(hostname, port);

        (new CommandHandler(serverAddress)).runREPL();
    }
}
