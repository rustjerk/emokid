package ru.sigsegv.lab7.common.serde.json;

import ru.sigsegv.lab7.common.serde.SerDe;
import ru.sigsegv.lab7.common.serde.SerDeUtils;
import ru.sigsegv.lab7.common.serde.Serializer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
     *
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
        var df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(340);
        buffer.append(df.format(val));
    }

    @Override
    public void serializeString(String val) {
        buffer.append(SerDeUtils.enquote(val));
    }

    @Override
    public Map serializeMap() {
        buffer.append("{");
        increaseIndent();

        return new Map() {
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
            public void serializeValue(Object value) {
                SerDe.serialize(JsonPrettySerializer.this, value);
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
    public Seq serializeSeq() {
        buffer.append("[");
        increaseIndent();

        return new Seq() {
            private boolean isFirst = true;

            @Override
            public void serializeValue(Object value) {
                buffer.append(isFirst ? "\n" : ",\n");
                isFirst = false;
                buffer.append(curIndent);
                SerDe.serialize(JsonPrettySerializer.this, value);
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
