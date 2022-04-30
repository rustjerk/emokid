package ru.sigsegv.lab7.common.serde;

@FunctionalInterface
public interface TypeSerializer<T> {
    void serialize(Serializer serializer, T value);
}
