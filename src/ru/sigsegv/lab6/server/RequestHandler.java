package ru.sigsegv.lab6.server;

import ru.sigsegv.lab6.common.Request;
import ru.sigsegv.lab6.common.Response;

@FunctionalInterface
public interface RequestHandler {
    Response<?> handle(Request<?> request);
}
