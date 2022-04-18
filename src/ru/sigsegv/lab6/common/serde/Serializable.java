package ru.sigsegv.lab6.common.serde;

/**
 * Objects serializable by reference
 */
public interface Serializable extends java.io.Serializable {
    /**
     * Serialize the object
     * @param serializer serializer
     */
    void serialize(Serializer serializer);
}
