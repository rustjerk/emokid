package ru.sigsegv.emokid.server;

import java.io.IOException;

public abstract class Server {
    protected final ServerContext context;

    public Server(ServerContext context) throws IOException {
        this.context = context;
    }

    public abstract void serve() throws IOException;
}
