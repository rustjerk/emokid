package ru.sigsegv.lab7.common.serde;

public interface Deserializer {
    default DataType getHint() throws DeserializeException {
        return null;
    }

    boolean deserializeBoolean() throws DeserializeException;

    long deserializeLong() throws DeserializeException;

    double deserializeDouble() throws DeserializeException;

    String deserializeString() throws DeserializeException;

    Map deserializeMap() throws DeserializeException;

    Seq deserializeSeq() throws DeserializeException;

    default String formatErrorMessage(String fmt, Object... args) {
        return String.format(fmt, args);
    }

    default void setHelp(String help) {
    }

    @SuppressWarnings("RedundantThrows")
    interface Map {
        String nextKey(String keyHint, boolean isRequired) throws DeserializeException;

        <T> T nextValue(Class<T> type) throws DeserializeException;

        default boolean retryValue(Throwable cause) throws DeserializeException {
            return false;
        }

        default void finishValue() throws DeserializeException {
        }

        void finish() throws DeserializeException;
    }

    interface Seq {
        boolean hasNext() throws DeserializeException;

        <T> T nextValue(Class<T> type) throws DeserializeException;

        void finish() throws DeserializeException;
    }
}
