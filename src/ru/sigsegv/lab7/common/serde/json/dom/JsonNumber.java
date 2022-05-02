package ru.sigsegv.lab7.common.serde.json.dom;

public class JsonNumber extends JsonValue {
    public double inner;

    public JsonNumber(double inner) {
        this.inner = inner;
    }
}
