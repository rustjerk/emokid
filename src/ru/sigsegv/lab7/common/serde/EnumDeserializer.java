package ru.sigsegv.lab7.common.serde;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumDeserializer<T extends Enum<T>> implements TypeDeserializer<T> {
    private final Map<String, T> constants;

    public EnumDeserializer(Class<T> type) {
        constants = Arrays.stream(type.getEnumConstants())
                .collect(Collectors.toMap(Object::toString, x -> x));
    }

    @Override
    public T deserialize(Deserializer deserializer) throws DeserializeException {
        deserializer.setHelp("variants: " + constants.values().stream()
                .map(Object::toString).collect(Collectors.joining(", ")));

        var str = deserializer.deserializeString();
        var val = constants.get(str);
        if (val == null)
            throw new DeserializeException(deserializer.formatErrorMessage("invalid variant: " + str));
        return val;
    }
}
