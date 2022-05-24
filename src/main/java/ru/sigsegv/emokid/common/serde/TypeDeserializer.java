package ru.sigsegv.emokid.common.serde;

@FunctionalInterface
public interface TypeDeserializer<T> {
    T deserialize(Deserializer deserializer) throws DeserializeException;
}
