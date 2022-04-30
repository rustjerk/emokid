package ru.sigsegv.lab7.server;

import java.io.IOException;
import java.net.SocketAddress;
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

        for (var key : selector.selectedKeys()) {
            for (var module : modules) {
                module.handleSelectedKey(key);
            }
        }

        for (var module : modules) {
            module.update(selector);
        }
    }
}
