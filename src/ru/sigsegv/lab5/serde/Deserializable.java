package ru.sigsegv.lab5.serde;

/**
 * Objects, deserializable by reference
 */
@FunctionalInterface
public interface Deserializable {
    /**
     * Deserializes the object
     * @param deserializer a given deserializer
     * @throws DeserializeException if there is an IO error, or the input is malformed
     */
    void deserialize(Deserializer deserializer) throws DeserializeException;
}
