package ru.sigsegv.emokid.common.serde;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordSerializer<T extends Record> implements TypeSerializer<T> {
    private final Map<String, Entry> fields;

    public RecordSerializer(Class<T> type) {
        fields = Arrays.stream(type.getRecordComponents())
                .collect(Collectors.toMap(RecordComponent::getName,
                        r -> new Entry(r.getAccessor(), r.getType()), (x, y) -> y, LinkedHashMap::new));
    }

    @Override
    public void serialize(Serializer serializer, T source) {
        var map = serializer.serializeMap();

        for (var entry : fields.entrySet()) {
            try {
                var key = entry.getKey();
                var value = entry.getValue().accessor.invoke(source);

                if (value == null) continue;
                map.serializeKey(key);
                map.serializeValue(value, entry.getValue().type);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("reflection error: " + e.getMessage());
            }
        }

        map.finish();
    }

    private record Entry(Method accessor, Class<?> type) {
    }
}
