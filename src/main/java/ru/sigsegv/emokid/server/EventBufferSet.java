package ru.sigsegv.emokid.server;

import java.util.HashSet;
import java.util.Set;

public class EventBufferSet {
    private final Set<EventBuffer> buffers = new HashSet<>();

    public synchronized void add(EventBuffer buffer) {
        buffers.add(buffer);
    }

    public synchronized void send(Object event) {
        buffers.forEach(b -> b.send(event));
    }

    public synchronized void delete(EventBuffer buffer) {
        buffers.remove(buffer);
    }
}
