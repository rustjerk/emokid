package ru.sigsegv.lab6.common.serde;

/**
 * Serializer for owned values (like integers, String, ZonedDateTime, etc)
 * @param <T> type of the value
 */
@FunctionalInterface
public interface OwnedSerializer<T> {
    /**
     * Serializes a value
     * @param serializer serializer
     * @param value value to serialize
     */
    void serialize(Serializer serializer, T value);
}
