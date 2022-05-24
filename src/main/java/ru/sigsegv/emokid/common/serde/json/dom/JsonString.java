package ru.sigsegv.emokid.common.serde.json.dom;

public class JsonString extends JsonValue {
    public String inner;

    public JsonString(String inner) {
        this.inner = inner;
    }
}
