package ru.sigsegv.lab7.server;

import ru.sigsegv.lab7.common.Request;
import ru.sigsegv.lab7.common.Response;

@FunctionalInterface
public interface RequestHandler {
    Response<?> handle(Request<?> request);
}
