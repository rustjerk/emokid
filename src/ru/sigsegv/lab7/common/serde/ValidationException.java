package ru.sigsegv.lab7.common.serde;

public class ValidationException extends Exception {
    public ValidationException(String reason) {
        super(reason);
    }
}
