package ru.sigsegv.lab5.serde;

import ru.sigsegv.lab5.util.Nullable;

import java.lang.reflect.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserializer of struct-like classes
 *
 * @param <T> type of the class
 */
public class StructDeserializer<T> {
    private final Map<String, FieldEntry> fields = new LinkedHashMap<>();

    /**
     * @param clazz class to deserialize
     */
    public StructDeserializer(Class<T> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            String name = field.getName();
            boolean isPublic = Modifier.isPublic(field.getModifiers());
            boolean isNullable = field.getAnnotation(Nullable.class) != null;

            Method setter = SerDeUtils.getPublicMethod(clazz, SerDeUtils.getSetterName(name), type);
            Method getter = SerDeUtils.getPublicMethod(clazz, SerDeUtils.getGetterName(name));
            Constructor<?> ctor = SerDeUtils.getPublicCtor(type);

            boolean isDeserializable = Deserializable.class.isAssignableFrom(type);
            OwnedDeserializer<?> ownedDeserializer = OwnedDeserializers.get(type);
            FieldDeserializer deserializer;

            if (isDeserializable && isNullable && getter == null && setter == null && ctor != null && isPublic) {
                deserializer = (d, o) -> {
                    if (field.get(o) == null) {
                        field.set(o, ctor.newInstance());
                    }

                    ((Deserializable) field.get(o)).deserialize(d);
                };
            } else if (isDeserializable && isNullable && getter != null && setter != null && ctor != null) {
                deserializer = (d, o) -> {
                    if (getter.invoke(o) == null) {
                        setter.invoke(o, ctor.newInstance());
                    }

                    ((Deserializable) getter.invoke(o)).deserialize(d);
                };
            } else if (isDeserializable && isNullable && getter == null && isPublic) {
                deserializer = (d, o) -> {
                    Object v = field.get(o);
                    if (v != null) {
                        ((Deserializable) v).deserialize(d);
                    }
                };
            } else if (isDeserializable && !isNullable && getter == null && isPublic) {
                deserializer = (d, o) -> ((Deserializable) field.get(o)).deserialize(d);
            } else if (isDeserializable && !isNullable && getter != null) {
                deserializer = (d, o) -> ((Deserializable) getter.invoke(o)).deserialize(d);
            } else if (ownedDeserializer != null && isPublic) {
                deserializer = (d, o) -> field.set(o, ownedDeserializer.deserialize(d));
            } else if (ownedDeserializer != null && setter != null) {
                deserializer = (d, o) -> setter.invoke(o, ownedDeserializer.deserialize(d));
            } else {
                continue;
            }

            SkipDeserialization ann = field.getAnnotation(SkipDeserialization.class);

            FieldEntry entry = new FieldEntry();
            entry.isRequired = !isNullable;
            entry.skipDeserializer = ann == null ? null : ann.deserializer();
            entry.deserializer = deserializer;
            fields.put(name, entry);
        }
    }

    /**
     * Deserialize a struct-like class
     *
     * @param deserializer deserializer
     * @param target       reference of the object to deserialize into
     * @throws DeserializeException if the input has errors
     */
    public void deserialize(Deserializer deserializer, T target) throws DeserializeException {
        Map<String, FieldEntry> remainingFields = new LinkedHashMap<>(fields);

        Deserializer.Map map = deserializer.deserializeMap();

        while (!remainingFields.isEmpty()) {
            Map.Entry<String, FieldEntry> entry = remainingFields.entrySet().stream().findFirst().get();
            String expectedKey = entry.getKey();
            FieldEntry expectedField = entry.getValue();

            if (expectedField.skipDeserializer == deserializer.getClass()) {
                remainingFields.remove(expectedKey);
                continue;
            }

            String actualKey = map.nextKey(expectedKey, expectedField.isRequired);

            if (actualKey == null) {
                if (expectedField.isRequired) {
                    throw new DeserializeException(
                            deserializer.formatErrorMessage("key `%s` is not present", expectedKey));
                }

                remainingFields.remove(expectedKey);
                continue;
            }

            FieldEntry actualField = remainingFields.remove(actualKey);
            if (actualField == null)
                throw new DeserializeException(deserializer.formatErrorMessage("unexpected field `%s`", actualKey));

            map.nextValue(d -> {
                try {
                    actualField.deserializer.deserializeField(d, target);
                } catch (InvocationTargetException e) {
                    throw new DeserializeException(d.formatErrorMessage("%s", e.getCause().getMessage()));
                } catch (ReflectiveOperationException e) {
                    throw new DeserializeException(d.formatErrorMessage("reflection error: %s", e.toString()));
                }
            });
        }

        map.finish();
    }

    private static class FieldEntry {
        boolean isRequired;
        Class<?> skipDeserializer;
        FieldDeserializer deserializer;
    }

    @FunctionalInterface
    private interface FieldDeserializer {
        void deserializeField(Deserializer deserializer, Object target)
                throws DeserializeException, ReflectiveOperationException;
    }
}
