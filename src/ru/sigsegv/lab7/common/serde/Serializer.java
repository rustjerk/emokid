package ru.sigsegv.lab7.common.serde;

public interface Serializer {
    void serializeBoolean(boolean val);

    void serializeLong(long val);

    void serializeDouble(double val);

    void serializeString(String val);

    Map serializeMap();

    Seq serializeSeq();

    interface Map {
        void serializeKey(String key);

        void serializeValue(Object value);

        void finish();
    }

    interface Seq {
        void serializeValue(Object value);

        void finish();
    }
}
