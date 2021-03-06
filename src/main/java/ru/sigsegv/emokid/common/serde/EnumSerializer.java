package ru.sigsegv.emokid.common.serde;

public class EnumSerializer<T extends Enum<T>> implements TypeSerializer<T> {
    @Override
    public void serialize(Serializer serializer, T value) {
        serializer.serializeString(value.toString());
    }
}
