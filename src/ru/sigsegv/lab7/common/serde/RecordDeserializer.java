package ru.sigsegv.lab7.common.serde;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecordDeserializer<T extends Record> implements TypeDeserializer<T> {
    private final Constructor<T> constructor;

    private final Map<String, FieldEntry> fields = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public RecordDeserializer(Class<T> type) {
        constructor = getCanonicalConstructor(type);

        var index = 0;
        for (var field : type.getRecordComponents()) {
            var isRequired = field.getAnnotation(Nullable.class) == null;

            var annD = field.getAnnotation(SkipDeserialization.class);
            var skipDeserializer = annD == null ? null : annD.deserializer();

            var annV = field.getAnnotation(Validate.class);

            Validator<Object> validator = null;
            if (annV != null) {
                try {
                    validator = (Validator<Object>) annV.value().getConstructor().newInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }

            var entry = new FieldEntry(index, field.getType(), isRequired, skipDeserializer, validator);
            fields.put(field.getName(), entry);
            index += 1;
        }
    }

    private static <T extends Record> Constructor<T> getCanonicalConstructor(Class<T> cls) {
        var paramTypes =
                Arrays.stream(cls.getRecordComponents())
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
        try {
            return cls.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public T deserialize(Deserializer deserializer) throws DeserializeException {
        var remainingFields = new LinkedHashMap<>(fields);
        var values = new Object[fields.size()];

        var map = deserializer.deserializeMap();

        while (!remainingFields.isEmpty()) {
            var entry = remainingFields.entrySet().stream().findFirst().get();
            var expectedKey = entry.getKey();
            var expectedField = entry.getValue();

            if (expectedField.skipDeserializer == deserializer.getClass()) {
                if (expectedField.isRequired)
                    values[expectedField.index] = defaultInstance(expectedField.type);
                remainingFields.remove(expectedKey);
                continue;
            }

            var actualKey = map.nextKey(expectedKey, expectedField.isRequired);
            if (actualKey == null) {
                if (expectedField.isRequired) {
                    throw new DeserializeException(
                            deserializer.formatErrorMessage("key `%s` is not present", expectedKey));
                }

                remainingFields.remove(expectedKey);
                continue;
            }

            var actualField = remainingFields.remove(actualKey);
            if (actualField == null)
                throw new DeserializeException(deserializer.formatErrorMessage("unexpected field `%s`", actualKey));

            var value = map.nextValue(actualField.type);
            if (actualField.validator != null) actualField.validator.validate(value);
            values[actualField.index] = value;
        }

        map.finish();

        try {
            return constructor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object defaultInstance(Class<?> type) {
        if (type == byte.class)
            return (byte) 0;
        if (type == short.class)
            return (short) 0;
        if (type == int.class)
            return 0;
        if (type == long.class)
            return (long) 0;
        return null;
    }

    private record FieldEntry(int index, Class<?> type, boolean isRequired,
                              Class<? extends Deserializer> skipDeserializer,
                              Validator<Object> validator) {
    }
}
