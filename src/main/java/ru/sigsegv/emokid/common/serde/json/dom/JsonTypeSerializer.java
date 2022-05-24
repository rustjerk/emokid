package ru.sigsegv.emokid.common.serde.json.dom;

import ru.sigsegv.emokid.common.serde.Serializer;
import ru.sigsegv.emokid.common.serde.TypeSerializer;

public class JsonTypeSerializer implements TypeSerializer<JsonValue> {
    @Override
    public void serialize(Serializer serializer, JsonValue value) {
        if (value instanceof JsonBoolean v)
            serializer.serializeBoolean(v.inner);
        else if (value instanceof JsonNumber v)
            serializer.serializeDouble(v.inner);
        else if (value instanceof JsonString v)
            serializer.serializeString(v.inner);
        else if (value instanceof JsonList v) {
            var list = serializer.serializeSeq();
            for (var item : v.inner) list.serializeValue(item);
            list.finish();
        } else if (value instanceof JsonMap v) {
            var map = serializer.serializeMap();
            for (var item : v.inner.entrySet()) {
                map.serializeKey(item.getKey());
                map.serializeValue(item.getValue());
            }
            map.finish();
        }
    }
}
