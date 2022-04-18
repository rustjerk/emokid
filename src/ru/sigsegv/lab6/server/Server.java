package ru.sigsegv.lab6.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class Server {
    private final Selector selector;
    private final ServerModule[] modules;

    public Server(SocketAddress address, RequestHandler requestHandler) throws IOException {
        selector = Selector.open();

        modules = new ServerModule[]{
                new ServerModuleTCP(address, requestHandler, selector),
                new ServerModuleUDP(address, requestHandler, selector)
        };
    }

    public void tick(int timeoutMs) throws IOException {
        selector.select(timeoutMs);

        for (SelectionKey key : selector.selectedKeys()) {
            for (ServerModule module : modules) {
                module.handleSelectedKey(key);
            }
        }

        for (ServerModule module : modules) {
            module.update(selector);
        }
    }
}
