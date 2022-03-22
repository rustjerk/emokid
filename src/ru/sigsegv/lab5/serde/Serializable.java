package ru.sigsegv.lab5.serde;

/**
 * Objects serializable by reference
 */
public interface Serializable {
    /**
     * Serialize the object
     * @param serializer serializer
     */
    void serialize(Serializer serializer);
}
