package ru.sigsegv.lab6.common.serde;

/**
 * Deserializer for a generic non-self-descriptive format
 */
public interface Deserializer {
    /**
     * Tries to deserialize a boolean
     *
     * @return a boolean
     * @throws DeserializeException if there is no boolean
     */
    boolean deserializeBoolean() throws DeserializeException;

    /**
     * Tries to deserialize a long
     *
     * @return a long
     * @throws DeserializeException if there is no long
     */
    long deserializeLong() throws DeserializeException;

    /**
     * Tries to deserialize a double
     *
     * @return a double
     * @throws DeserializeException if there is no double
     */
    double deserializeDouble() throws DeserializeException;

    /**
     * Tries to deserialize a string
     *
     * @return a string
     * @throws DeserializeException if there is no string
     */
    String deserializeString() throws DeserializeException;

    /**
     * Tries to deserialize a map
     *
     * @return map deserializer
     * @throws DeserializeException if there is no map
     */
    Map deserializeMap() throws DeserializeException;

    /**
     * Tries to deserialize a sequence
     *
     * @return sequence deserializer
     * @throws DeserializeException if there is no sequence
     */
    Seq deserializeSeq() throws DeserializeException;

    /**
     * Formats an error message, including the current deserializer location
     *
     * @param fmt  format string
     * @param args format arguments
     * @return error message
     */
    default String formatErrorMessage(String fmt, Object... args) {
        return String.format(fmt, args);
    }

    /**
     * Sets a help message to be shown during command-line deserialization
     *
     * @param help a message to be displayed during deserialization of the next simple field
     */
    default void setHelp(String help) {
    }

    /**
     * Map deserializer
     */
    interface Map {
        /**
         * Parses the next key
         *
         * @param keyHint    name of the next key
         * @param isRequired is the next key required
         * @return an actual key, or null if there are no more keys
         * @throws DeserializeException if the input is malformed
         */
        String nextKey(String keyHint, boolean isRequired) throws DeserializeException;

        /**
         * Parses the next value. Must be called after nextKey
         *
         * @param deserializable value to deserialize
         * @throws DeserializeException if the input is malformed
         */
        void nextValue(Deserializable deserializable) throws DeserializeException;

        /**
         * Finishes parsing the map. Must be called after all calls to nextKey and nextValue
         *
         * @throws DeserializeException if the input is malformed
         */
        void finish() throws DeserializeException;
    }

    /**
     * Sequence deserializer
     */
    interface Seq {
        /**
         * @return true if the sequence has more elements
         * @throws DeserializeException if the input is malformed
         */
        boolean hasNext() throws DeserializeException;

        /**
         * Parses the next value
         *
         * @param deserializable value to deserialize
         * @throws DeserializeException if the input is malformed
         */
        void nextValue(Deserializable deserializable) throws DeserializeException;

        /**
         * Finishes parsing the sequence. Must be called after all calls to nextValue
         *
         * @throws DeserializeException if the input is malformed
         */
        void finish() throws DeserializeException;
    }
}
