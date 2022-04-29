package ru.sigsegv.lab7.common.serde;

/**
 * Objects serializable by reference
 */
public interface Serializable extends java.io.Serializable {
    /**
     * Serialize the object
     *
     * @param serializer serializer
     */
    void serialize(Serializer serializer);
}
