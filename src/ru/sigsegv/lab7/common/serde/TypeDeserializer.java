package ru.sigsegv.lab7.common.serde;

@FunctionalInterface
public interface TypeDeserializer<T> {
    T deserialize(Deserializer deserializer) throws DeserializeException;
}
