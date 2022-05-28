package ru.sigsegv.emokid.common.serde;

public interface Serializer {
    void serializeBoolean(boolean val);

    void serializeLong(long val);

    void serializeDouble(double val);

    void serializeString(String val);

    Map serializeMap();

    Seq serializeSeq();

    interface Map {
        void serializeKey(String key);

        default void serializeValue(Object value) {
            serializeValue(value, value.getClass());
        }

        void serializeValue(Object value, Class<?> type);

        void finish();
    }

    interface Seq {
        default void serializeValue(Object value) {
            serializeValue(value, value.getClass());
        }

        void serializeValue(Object value, Class<?> type);

        void finish();
    }
}
