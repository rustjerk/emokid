package ru.sigsegv.emokid.common.serde;

@FunctionalInterface
public interface TypeSerializer<T> {
    void serialize(Serializer serializer, T value);
}
