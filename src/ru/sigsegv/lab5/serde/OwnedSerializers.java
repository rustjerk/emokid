package ru.sigsegv.lab5.serde;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Set of all registered owned serializers
 */
public class OwnedSerializers {
    private final static HashMap<Class<?>, OwnedSerializer<?>> serializers = new HashMap<>();

    /**
     * Gets a serializer
     * @param clazz serializable class
     * @param <T> type of the class
     * @return serializer, or null if it doesn't exist
     */
    public static <T> OwnedSerializer<T> get(Class<T> clazz) {
        return (OwnedSerializer<T>) serializers.get(clazz);
    }

    /**
     * Registers a serializer
     * @param clazz serializable class
     * @param serializer serializer
     * @param <T> type of the class
     */
    public static <T> void register(Class<T> clazz, OwnedSerializer<T> serializer) {
        serializers.put(clazz, serializer);
    }

    static {
        register(boolean.class, Serializer::serializeBoolean);
        register(Boolean.class, Serializer::serializeBoolean);

        register(byte.class, (s, v) -> s.serializeLong(v));
        register(Byte.class, (s, v) -> s.serializeLong(v));

        register(short.class, (s, v) -> s.serializeLong(v));
        register(Short.class, (s, v) -> s.serializeLong(v));

        register(int.class, (s, v) -> s.serializeLong(v));
        register(Integer.class, (s, v) -> s.serializeLong(v));

        register(long.class, Serializer::serializeLong);
        register(Long.class, Serializer::serializeLong);

        register(float.class, (s, v) -> s.serializeDouble(v));
        register(Float.class, (s, v) -> s.serializeDouble(v));

        register(double.class, Serializer::serializeDouble);
        register(Double.class, Serializer::serializeDouble);

        register(String.class, Serializer::serializeString);

        register(ZonedDateTime.class, (s, v) -> s.serializeString(v.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
    }
}
