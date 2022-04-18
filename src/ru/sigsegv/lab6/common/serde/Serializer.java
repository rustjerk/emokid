package ru.sigsegv.lab6.common.serde;

/**
 * Generic serializer
 */
public interface Serializer {
    /**
     * Serializes a boolean
     * @param val value to serialize
     */
    void serializeBoolean(boolean val);

    /**
     * Serializes a long
     * @param val value to serialize
     */
    void serializeLong(long val);

    /**
     * Serializes a double
     * @param val value to serialize
     */
    void serializeDouble(double val);

    /**
     * Serializes a string
     * @param val value to serialize
     */
    void serializeString(String val);

    /**
     * Serializes a map
     * @return map serializer
     */
    Map serializeMap();

    /**
     * Serializes a sequence
     * @return sequence serializer
     */
    Seq serializeSeq();

    /**
     * Map serializer
     */
    interface Map {
        /**
         * Serializes a key
         * @param key key to serialize
         */
        void serializeKey(String key);

        /**
         * Serializes a value. Must be called after serializeKey
         * @param value value to serialize
         */
        void serializeValue(Serializable value);

        /**
         * Finishes serialization. Must be called after all serializeValue calls
         */
        void finish();
    }

    interface Seq {
        void serializeValue(Serializable value);

        void finish();
    }
}
