package ru.sigsegv.emokid.common.serde;

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
