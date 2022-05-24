package ru.sigsegv.emokid.common.serde;

import ru.sigsegv.emokid.common.serde.json.dom.JsonTypeDeserializer;
import ru.sigsegv.emokid.common.serde.json.dom.JsonTypeSerializer;
import ru.sigsegv.emokid.common.serde.json.dom.JsonValue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class SerDe {
    private static final Map<Class<?>, TypeSerializer<?>> typeSerializers = new HashMap<>();
    private static final Map<Class<?>, TypeDeserializer<?>> typeDeserializers = new HashMap<>();

    static {
        registerSerializer(Boolean.class, Serializer::serializeBoolean);
        registerDeserializer(Boolean.class, Deserializer::deserializeBoolean);
        registerSerializer(boolean.class, Serializer::serializeBoolean);
        registerDeserializer(boolean.class, Deserializer::deserializeBoolean);

        registerSerializer(Byte.class, (s, v) -> s.serializeLong(v));
        registerDeserializer(Byte.class, d -> (byte) d.deserializeLong());
        registerSerializer(byte.class, (s, v) -> s.serializeLong(v));
        registerDeserializer(byte.class, d -> (byte) d.deserializeLong());

        registerSerializer(Short.class, (s, v) -> s.serializeLong(v));
        registerDeserializer(Short.class, d -> (short) d.deserializeLong());
        registerSerializer(short.class, (s, v) -> s.serializeLong(v));
        registerDeserializer(short.class, d -> (short) d.deserializeLong());

        registerSerializer(Integer.class, (s, v) -> s.serializeLong(v));
        registerDeserializer(Integer.class, d -> (int) d.deserializeLong());
        registerSerializer(int.class, (s, v) -> s.serializeLong(v));
        registerDeserializer(int.class, d -> (int) d.deserializeLong());

        registerSerializer(Long.class, Serializer::serializeLong);
        registerDeserializer(Long.class, Deserializer::deserializeLong);
        registerSerializer(long.class, Serializer::serializeLong);
        registerDeserializer(long.class, Deserializer::deserializeLong);

        registerSerializer(Float.class, (s, v) -> s.serializeDouble(v));
        registerDeserializer(Float.class, d -> (float) d.deserializeDouble());
        registerSerializer(float.class, (s, v) -> s.serializeDouble(v));
        registerDeserializer(float.class, d -> (float) d.deserializeDouble());

        registerSerializer(Double.class, Serializer::serializeDouble);
        registerDeserializer(Double.class, Deserializer::deserializeDouble);
        registerSerializer(double.class, Serializer::serializeDouble);
        registerDeserializer(double.class, Deserializer::deserializeDouble);

        registerSerializer(String.class, Serializer::serializeString);
        registerDeserializer(String.class, Deserializer::deserializeString);

        registerSerializer(JsonValue.class, new JsonTypeSerializer());
        registerDeserializer(JsonValue.class, new JsonTypeDeserializer());

        registerSerializer(ZonedDateTime.class, (s, v) -> s.serializeString(v.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
        registerDeserializer(ZonedDateTime.class, d -> {
            try {
                d.setHelp("example: 2007-12-03T10:15:30+01:00[Europe/Paris]");
                return ZonedDateTime.parse(d.deserializeString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new DeserializeException(d.formatErrorMessage(e.getMessage()));
            }
        });
    }

    public static <T> void registerSerializer(Class<T> type, TypeSerializer<T> serializer) {
        typeSerializers.put(type, serializer);
    }

    public static <T> void registerDeserializer(Class<T> type, TypeDeserializer<T> deserializer) {
        typeDeserializers.put(type, deserializer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> TypeSerializer<T> getSerializer(Class<T> type) {
        var ser = (TypeSerializer<T>) typeSerializers.computeIfAbsent(type, t -> {
            if (type.isRecord())
                return new RecordSerializer(t);
            if (type.isEnum())
                return new EnumSerializer();
            return null;
        });

        if (ser == null && !type.equals(Object.class)) {
            return (TypeSerializer<T>) getSerializer(type.getSuperclass());
        }

        return ser;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> TypeDeserializer<T> getDeserializer(Class<T> type) {
        return (TypeDeserializer<T>) typeDeserializers.computeIfAbsent(type, t -> {
            if (type.isRecord())
                return new RecordDeserializer(t);
            if (type.isEnum())
                return new EnumDeserializer(t);
            throw new UnsupportedOperationException("cannot deserialize " + t.getName());
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> void serialize(Serializer serializer, T value) {
        getSerializer((Class<T>) value.getClass()).serialize(serializer, value);
    }

    public static <T> T deserialize(Deserializer deserializer, Class<T> type) throws DeserializeException {
        return getDeserializer(type).deserialize(deserializer);
    }
}
