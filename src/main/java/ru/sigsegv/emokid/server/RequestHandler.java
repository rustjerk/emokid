package ru.sigsegv.emokid.server;

import ru.sigsegv.emokid.common.Request;
import ru.sigsegv.emokid.common.Response;

@FunctionalInterface
public interface RequestHandler {
    Response<?> handle(Request<?> request);
}
