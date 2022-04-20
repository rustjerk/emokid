package ru.sigsegv.lab6.common.serde;

/**
 * Deserializer for owned values (like integers, String, ZonedDateTime, etc)
 *
 * @param <T> type of the values
 */
@FunctionalInterface
public interface OwnedDeserializer<T> {
    /**
     * Deserializes a value.
     *
     * @param deserializer deserializer
     * @return deserialized value
     * @throws DeserializeException if the input has errors
     */
    T deserialize(Deserializer deserializer) throws DeserializeException;
}
