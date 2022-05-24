package ru.sigsegv.emokid.common.serde.json.dom;

import ru.sigsegv.emokid.common.serde.DeserializeException;
import ru.sigsegv.emokid.common.serde.Deserializer;
import ru.sigsegv.emokid.common.serde.TypeDeserializer;

public class JsonTypeDeserializer implements TypeDeserializer<JsonValue> {
    @Override
    public JsonValue deserialize(Deserializer deserializer) throws DeserializeException {
        return switch (deserializer.getHint()) {
            case BOOLEAN -> new JsonBoolean(deserializer.deserializeBoolean());
            case LONG -> new JsonNumber(deserializer.deserializeLong());
            case DOUBLE -> new JsonNumber(deserializer.deserializeDouble());
            case STRING -> new JsonString(deserializer.deserializeString());
            case MAP -> {
                var map = new JsonMap();
                var dMap = deserializer.deserializeMap();
                while (true) {
                    var key = dMap.nextKey(null, false);
                    if (key == null) break;
                    var value = dMap.nextValue(JsonValue.class);
                    dMap.finishValue();
                    map.put(key, value);
                }
                dMap.finish();
                yield map;
            }
            case SEQ -> {
                var list = new JsonList();
                var seq = deserializer.deserializeSeq();
                while (seq.hasNext()) {
                    list.add(seq.nextValue(JsonValue.class));
                }
                seq.finish();
                yield list;
            }
        };
    }
}
