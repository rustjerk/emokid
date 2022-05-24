package ru.sigsegv.emokid.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerContext {
    public final ExecutorService readExecutor = Executors.newCachedThreadPool();
    public final ExecutorService handleExecutor = Executors.newCachedThreadPool();
    public final ExecutorService writeExecutor = new ForkJoinPool();

    public final RequestHandler requestHandler;

    public final AtomicBoolean isRunning = new AtomicBoolean(true);

    public ServerContext(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void stop() {
        isRunning.set(false);
    }
}
