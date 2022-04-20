package ru.sigsegv.lab6.common.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializer of struct-like classes
 *
 * @param <T> type of the class
 */
public class StructSerializer<T> {
    private final Map<String, FieldEntry> fields = new LinkedHashMap<>();

    /**
     * @param clazz class to serialize
     */
    public StructSerializer(Class<T> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            String name = field.getName();

            Method getter = SerDeUtils.getPublicMethod(clazz, SerDeUtils.getGetterName(name));

            boolean isPublic = Modifier.isPublic(field.getModifiers());
            boolean isSerializable = Serializable.class.isAssignableFrom(type);

            if (getter == null && !isPublic) continue;

            OwnedSerializer<Object> ownedSerializer = (OwnedSerializer<Object>) OwnedSerializers.get(type);

            FieldNullPredicate nullPredicate = getter == null
                    ? (o) -> field.get(o) == null
                    : (o) -> getter.invoke(o) == null;

            FieldSerializer serializer;

            if (isSerializable) {
                serializer = getter == null
                        ? (s, o) -> ((Serializable) field.get(o)).serialize(s)
                        : (s, o) -> ((Serializable) getter.invoke(o)).serialize(s);
            } else if (ownedSerializer != null) {
                serializer = getter == null
                        ? (s, o) -> ownedSerializer.serialize(s, field.get(o))
                        : (s, o) -> ownedSerializer.serialize(s, getter.invoke(o));
            } else {
                continue;
            }

            FieldEntry entry = new FieldEntry();
            entry.nullPredicate = nullPredicate;
            entry.serializer = serializer;
            fields.put(name, entry);
        }
    }

    /**
     * Serializes a struct-like class instance
     *
     * @param serializer serializer
     * @param source     reference of the object to serialize
     */
    public void serialize(Serializer serializer, T source) {
        Serializer.Map map = serializer.serializeMap();

        for (Map.Entry<String, FieldEntry> entry : fields.entrySet()) {
            String key = entry.getKey();
            FieldEntry value = entry.getValue();

            try {
                if (value.nullPredicate.isNullField(source)) continue;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("reflection error: " + e.getMessage());
            }

            map.serializeKey(key);
            map.serializeValue(s -> {
                try {
                    value.serializer.serializeField(s, source);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("reflection error: " + e.getMessage());
                }
            });
        }

        map.finish();
    }

    private static class FieldEntry {
        FieldNullPredicate nullPredicate;
        FieldSerializer serializer;
    }

    @FunctionalInterface
    private interface FieldNullPredicate {
        boolean isNullField(Object source)
                throws ReflectiveOperationException;
    }

    @FunctionalInterface
    private interface FieldSerializer {
        void serializeField(Serializer serializer, Object source)
                throws ReflectiveOperationException;
    }
}
