package ru.sigsegv.lab7.common.serde.json;

import ru.sigsegv.lab7.common.serde.Deserializable;
import ru.sigsegv.lab7.common.serde.DeserializeException;
import ru.sigsegv.lab7.common.serde.Deserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.UnaryOperator;

/**
 * JSON format deserializer
 */
public class JsonDeserializer implements Deserializer {
    private final JsonTokenizer tokenizer;
    private JsonToken savedToken;
    private final List<String> locations = new ArrayList<>();

    /**
     * @param scanner scanner of the input
     */
    public JsonDeserializer(Scanner scanner) {
        tokenizer = new JsonTokenizer(scanner);
        savedToken = null;
    }

    @Override
    public boolean deserializeBoolean() throws DeserializeException {
        switch (next()) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
                throw new DeserializeException(formatErrorMessage("boolean expected"));
        }
    }

    @Override
    public long deserializeLong() throws DeserializeException {
        expect(JsonToken.NUMBER, "integer expected");
        return (long) tokenizer.getNumber();
    }

    @Override
    public double deserializeDouble() throws DeserializeException {
        expect(JsonToken.NUMBER, "float expected");
        return (long) tokenizer.getNumber();
    }

    @Override
    public String deserializeString() throws DeserializeException {
        expect(JsonToken.STRING, "string expected");
        return tokenizer.getString();
    }

    @Override
    public Deserializer.Map deserializeMap() throws DeserializeException {
        expect(JsonToken.L_BRACE, "`{` expected");

        return new Deserializer.Map() {
            private boolean isFirst = true;

            @Override
            public String nextKey(String keyHint, boolean isRequired) throws DeserializeException {
                if (peek() == JsonToken.R_BRACE) {
                    return null;
                }

                if (!isFirst) expect(JsonToken.COMMA, "`,` expected");
                expect(JsonToken.STRING, "string expected");
                isFirst = false;

                String key = tokenizer.getString();
                pushLocation(key, s -> String.format("%s.%s", s, key));
                return key;
            }

            @Override
            public void nextValue(Deserializable deserializable) throws DeserializeException {
                expect(JsonToken.COLON, "`:` expected");
                deserializable.deserialize(JsonDeserializer.this);
                popLocation();
            }

            @Override
            public void finish() throws DeserializeException {
                expect(JsonToken.R_BRACE, "`}` expected");
            }
        };
    }

    @Override
    public Deserializer.Seq deserializeSeq() throws DeserializeException {
        expect(JsonToken.L_BRACKET, "`[` expected");

        return new Deserializer.Seq() {
            private int index = 0;

            @Override
            public boolean hasNext() throws DeserializeException {
                return peek() != JsonToken.R_BRACKET;
            }

            @Override
            public void nextValue(Deserializable deserializable) throws DeserializeException {
                if (index > 0) expect(JsonToken.COMMA, "`,` expected");
                String iStr = String.format("[%d]", index);
                pushLocation(iStr, s -> s + iStr);
                deserializable.deserialize(JsonDeserializer.this);
                popLocation();
                index += 1;
            }

            @Override
            public void finish() throws DeserializeException {
                expect(JsonToken.R_BRACKET, "`]` expected");
            }
        };
    }

    @Override
    public String formatErrorMessage(String fmt, Object... args) {
        String postfix = String.format(fmt, args);
        return getLocation() == null ? postfix : String.format("%s: %s", getLocation(), postfix);
    }

    private String getLocation() {
        if (locations.isEmpty()) return null;
        return locations.get(locations.size() - 1);
    }

    private void pushLocation(String location) {
        locations.add(location);
    }

    private void pushLocation(String first, UnaryOperator<String> nextOp) {
        pushLocation(getLocation() == null ? first : nextOp.apply(getLocation()));
    }

    private void popLocation() {
        locations.remove(locations.size() - 1);
    }

    private void expect(JsonToken expected, String message) throws DeserializeException {
        JsonToken tok = next();
        if (tok != expected)
            throw new DeserializeException(formatErrorMessage(message));
    }

    private JsonToken peek() throws DeserializeException {
        if (savedToken == null)
            savedToken = tokenizer.nextToken();
        return savedToken;
    }

    private JsonToken next() throws DeserializeException {
        if (savedToken == null) {
            return tokenizer.nextToken();
        } else {
            JsonToken token = savedToken;
            savedToken = null;
            return token;
        }
    }
}
