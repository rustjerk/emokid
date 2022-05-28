package ru.sigsegv.emokid.common;

import ru.sigsegv.emokid.common.serde.Nullable;

public record Response<T>(Status status, @Nullable T payload) {
    public static Response<String> success() {
        return new Response<>(Status.SUCCESS, null);
    }

    public static <T> Response<T> success(T payload) {
        return new Response<>(Status.SUCCESS, payload);
    }

    public static Response<String> invalidRequest() {
        return new Response<>(Status.ERROR, "invalid request");
    }

    public static Response<String> unauthenticated() {
        return new Response<>(Status.ERROR, "unauthenticated");
    }

    public static Response<String> noSuchUser() {
        return new Response<>(Status.ERROR, "no such user");
    }

    public static Response<String> invalidPassword() {
        return new Response<>(Status.ERROR, "invalid password");
    }

    public static Response<String> usernameTaken() {
        return new Response<>(Status.ERROR, "username already taken");
    }

    public static Response<Exception> exception(Exception e) {
        return new Response<>(Status.ERROR, e);
    }

    public boolean isSuccess() {
        return status() == Status.SUCCESS;
    }

    public boolean isError() {
        return status() == Status.ERROR;
    }

    public enum Status {
        SUCCESS,
        ERROR
    }
}
