package ru.sigsegv.emokid.common.serde.json;

import ru.sigsegv.emokid.common.serde.DataType;
import ru.sigsegv.emokid.common.serde.DeserializeException;
import ru.sigsegv.emokid.common.serde.Deserializer;
import ru.sigsegv.emokid.common.serde.SerDe;

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
    public DataType getHint() throws DeserializeException {
        return switch (peek()) {
            case TRUE, FALSE -> DataType.BOOLEAN;
            case L_BRACKET -> DataType.SEQ;
            case L_BRACE -> DataType.MAP;
            case NUMBER -> DataType.DOUBLE;
            case STRING -> DataType.STRING;
            default -> null;
        };
    }

    @Override
    public boolean deserializeBoolean() throws DeserializeException {
        return switch (next()) {
            case TRUE -> true;
            case FALSE -> false;
            default -> throw new DeserializeException(formatErrorMessage("boolean expected"));
        };
    }

    @Override
    public long deserializeLong() throws DeserializeException {
        expect(JsonToken.NUMBER, "integer expected");
        return (long) tokenizer.getNumber();
    }

    @Override
    public double deserializeDouble() throws DeserializeException {
        expect(JsonToken.NUMBER, "float expected");
        return tokenizer.getNumber();
    }

    @Override
    public String deserializeString() throws DeserializeException {
        expect(JsonToken.STRING, "string expected");
        return tokenizer.getString();
    }

    @Override
    public Map deserializeMap() throws DeserializeException {
        expect(JsonToken.L_BRACE, "`{` expected");

        return new Map() {
            private boolean isFirst = true;

            @Override
            public String nextKey(String keyHint, boolean isRequired) throws DeserializeException {
                if (peek() == JsonToken.R_BRACE) {
                    return null;
                }

                if (!isFirst) expect(JsonToken.COMMA, "`,` expected");
                expect(JsonToken.STRING, "string expected");
                isFirst = false;

                var key = tokenizer.getString();
                pushLocation(key, s -> String.format("%s.%s", s, key));
                return key;
            }

            @Override
            public <T> T nextValue(Class<T> type) throws DeserializeException {
                expect(JsonToken.COLON, "`:` expected");
                var value = SerDe.deserialize(JsonDeserializer.this, type);
                popLocation();

                return value;
            }

            @Override
            public void finish() throws DeserializeException {
                expect(JsonToken.R_BRACE, "`}` expected");
            }
        };
    }

    @Override
    public Seq deserializeSeq() throws DeserializeException {
        expect(JsonToken.L_BRACKET, "`[` expected");

        return new Seq() {
            private int index = 0;

            @Override
            public boolean hasNext() throws DeserializeException {
                return peek() != JsonToken.R_BRACKET;
            }

            @Override
            public <T> T nextValue(Class<T> type) throws DeserializeException {
                if (index > 0) expect(JsonToken.COMMA, "`,` expected");

                var iStr = String.format("[%d]", index);
                pushLocation(iStr, s -> s + iStr);
                var value = SerDe.deserialize(JsonDeserializer.this, type);
                popLocation();
                index += 1;

                return value;
            }

            @Override
            public void finish() throws DeserializeException {
                expect(JsonToken.R_BRACKET, "`]` expected");
            }
        };
    }

    @Override
    public String formatErrorMessage(String fmt, Object... args) {
        var postfix = String.format(fmt, args);
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
        var tok = next();
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
            var token = savedToken;
            savedToken = null;
            return token;
        }
    }
}
