package ru.sigsegv.emokid.common.serde;

public class ValidationException extends Exception {
    public ValidationException(String reason) {
        super(reason);
    }
}
