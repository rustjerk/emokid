package ru.sigsegv.emokid.common.serde.json.dom;

public class JsonBoolean extends JsonValue {
    public boolean inner;

    public JsonBoolean(boolean inner) {
        this.inner = inner;
    }
}
