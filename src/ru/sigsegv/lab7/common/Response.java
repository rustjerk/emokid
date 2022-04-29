package ru.sigsegv.lab7.common;

import java.io.Serializable;

public class Response<T> implements Serializable {
    private final Status status;
    private final T payload;

    public Response(Status status, T payload) {
        this.status = status;
        this.payload = payload;
    }

    public static Response<String> success() {
        return new Response<>(Status.SUCCESS, null);
    }

    public static <T> Response<T> success(T payload) {
        return new Response<>(Status.SUCCESS, payload);
    }

    public static Response<String> invalidRequest() {
        return new Response<>(Status.ERROR, "invalid request");
    }

    public static Response<Exception> exception(Exception e) {
        return new Response<>(Status.ERROR, e);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return getStatus() == Status.SUCCESS;
    }

    public boolean isError() {
        return getStatus() == Status.ERROR;
    }

    public T getPayload() {
        return payload;
    }

    public enum Status {
        SUCCESS,
        ERROR
    }
}
