package ru.sigsegv.emokid.server;

import java.util.ArrayList;
import java.util.List;

public class EventBuffer {
    private final List<Object> events = new ArrayList<>();

    public synchronized void send(Object event) {
        events.add(event);
        this.notify();
    }

    public synchronized List<Object> receive() {
        while (true) {
            try {
                this.wait();
                if (events.size() > 0) {
                    var batch = new ArrayList<>(events);
                    events.clear();
                    return batch;
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}
