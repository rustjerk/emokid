package ru.sigsegv.emokid.common.serde.json.dom;

import ru.sigsegv.emokid.common.serde.DeserializeException;
import ru.sigsegv.emokid.common.serde.SerDe;
import ru.sigsegv.emokid.common.serde.json.JsonDeserializer;
import ru.sigsegv.emokid.common.serde.json.JsonPrettySerializer;

import java.util.Scanner;

public class JsonValue {
    public static JsonValue fromString(String str) {
        try {
            var deserializer = new JsonDeserializer(new Scanner(str));
            return SerDe.deserialize(deserializer, JsonValue.class);
        } catch (DeserializeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isList() {
        return this instanceof JsonList;
    }

    public JsonList asList() {
        return (JsonList) this;
    }

    public boolean isMap() {
        return this instanceof JsonMap;
    }

    public JsonMap asMap() {
        return (JsonMap) this;
    }

    public boolean isBoolean() {
        return this instanceof JsonBoolean;
    }

    public boolean asBoolean() {
        return ((JsonBoolean) this).inner;
    }

    public boolean isNumber() {
        return this instanceof JsonNumber;
    }

    public double asNumber() {
        return ((JsonNumber) this).inner;
    }

    public int asInt() {
        return (int) asNumber();
    }

    public boolean isString() {
        return this instanceof JsonString;
    }

    public String asString() {
        return ((JsonString) this).inner;
    }

    @Override
    public String toString() {
        var ser = new JsonPrettySerializer();
        SerDe.serialize(ser, this);
        return ser.getString();
    }
}
