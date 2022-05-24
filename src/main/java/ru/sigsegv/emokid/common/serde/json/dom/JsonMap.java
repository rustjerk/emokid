package ru.sigsegv.emokid.common.serde.json.dom;

import java.util.HashMap;
import java.util.Map;

public class JsonMap extends JsonValue {
    public Map<String, JsonValue> inner = new HashMap<>();

    public JsonMap put(String key, JsonValue value) {
        inner.put(key, value);
        return this;
    }

    public JsonMap put(String key, int value) {
        return put(key, new JsonNumber(value));
    }

    public JsonMap put(String key, String value) {
        return put(key, new JsonString(value));
    }

    public boolean containsKey(String key) {
        return inner.containsKey(key);
    }

    public JsonValue get(String key) {
        return inner.get(key);
    }
}
