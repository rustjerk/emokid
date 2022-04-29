package ru.sigsegv.lab7.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface ServerModule {
    void update(Selector selector) throws IOException;

    void handleSelectedKey(SelectionKey key) throws IOException;
}
