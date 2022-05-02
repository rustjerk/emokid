package ru.sigsegv.lab7.common.serde.json.dom;

import java.util.ArrayList;
import java.util.List;

public class JsonList extends JsonValue {
    public List<JsonValue> inner = new ArrayList<>();

    public static JsonList of(JsonValue... values) {
        var list = new JsonList();
        list.inner.addAll(List.of(values));
        return list;
    }

    public int size() {
        return inner.size();
    }

    public JsonList add(JsonValue value) {
        inner.add(value);
        return this;
    }

    public JsonValue get(int index) {
        return inner.get(index);
    }
}
