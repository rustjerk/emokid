package ru.sigsegv.lab5.serde.json;

import ru.sigsegv.lab5.serde.SerDeUtils;
import ru.sigsegv.lab5.serde.Serializable;
import ru.sigsegv.lab5.serde.Serializer;

/**
 * JSON format serializer (pretty printer)
 */
public class JsonPrettySerializer implements Serializer {
    private final String singleIndent;
    private String curIndent;

    private final StringBuffer buffer = new StringBuffer();

    /**
     * @param indent a string with any number of spaces or tabs used for indentation
     */
    public JsonPrettySerializer(String indent) {
        this.singleIndent = indent;
        curIndent = "";
    }

    /**
     * Uses default indentation of two spaces
     */
    public JsonPrettySerializer() {
        this("  ");
    }

    private void increaseIndent() {
        curIndent += singleIndent;
    }

    private void decreaseIndent() {
        curIndent = curIndent.substring(0, curIndent.length() - singleIndent.length());
    }

    /**
     * Gets the resulting string.
     * @return the resulting string
     */
    public String getString() {
        return buffer.toString();
    }

    @Override
    public void serializeBoolean(boolean val) {
        buffer.append(val ? "true" : false);
    }

    @Override
    public void serializeLong(long val) {
        buffer.append(val);
    }

    @Override
    public void serializeDouble(double val) {
        buffer.append(val);
    }

    @Override
    public void serializeString(String val) {
        buffer.append(SerDeUtils.enquote(val));
    }

    @Override
    public Serializer.Map serializeMap() {
        buffer.append("{");
        increaseIndent();

        return new Serializer.Map() {
            private boolean isFirst = true;

            @Override
            public void serializeKey(String key) {
                buffer.append(isFirst ? "\n" : ",\n");
                buffer.append(curIndent);
                buffer.append(SerDeUtils.enquote(key));
                buffer.append(": ");
                isFirst = false;
            }

            @Override
            public void serializeValue(Serializable value) {
                value.serialize(JsonPrettySerializer.this);
            }

            @Override
            public void finish() {
                if (!isFirst) {
                    buffer.append("\n");
                    decreaseIndent();
                    buffer.append(curIndent);
                }

                buffer.append("}");
            }
        };
    }

    @Override
    public Serializer.Seq serializeSeq() {
        buffer.append("[");
        increaseIndent();

        return new Serializer.Seq() {
            private boolean isFirst = true;

            @Override
            public void serializeValue(Serializable value) {
                buffer.append(isFirst ? "\n" : ",\n");
                isFirst = false;
                buffer.append(curIndent);
                value.serialize(JsonPrettySerializer.this);
            }

            @Override
            public void finish() {
                if (!isFirst) {
                    buffer.append("\n");
                    decreaseIndent();
                    buffer.append(curIndent);
                }

                increaseIndent();
                buffer.append("]");
            }
        };
    }
}
