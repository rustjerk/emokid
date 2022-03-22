package ru.sigsegv.lab5.serde;

/**
 * An exception happened during deserialization
 */
public class DeserializeException extends Exception {
    /**
     * @param msg error message
     */
    public DeserializeException(String msg) {
        super(msg);
    }
}
