package ru.sigsegv.lab5.serde;

import ru.sigsegv.lab5.command.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Command-line deserializer
 */
public class CommandDeserializer implements Deserializer {
    private final CommandInput input;
    private final List<String> locations = new ArrayList<>();
    private String help;

    /**
     * @param input command input
     */
    public CommandDeserializer(CommandInput input) {
        this.input = input;
    }

    @Override
    public boolean deserializeBoolean() throws DeserializeException {
        while (true) {
            String line = readLine(getFieldPrompt("true/false")).toLowerCase();
            if (!line.isEmpty()) {
                if ("true".startsWith(line)) return true;
                if ("false".startsWith(line)) return false;
            }
            System.out.println("Invalid input. Please try again.");
        }
    }

    @Override
    public long deserializeLong() throws DeserializeException {
        while (true) {
            String line = readLine(getFieldPrompt("integer"));
            try {
                return Long.parseLong(line);
            } catch (NumberFormatException e) {
                invalidInput();
            }
        }
    }

    @Override
    public double deserializeDouble() throws DeserializeException {
        while (true) {
            String line = readLine(getFieldPrompt("float"));
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                invalidInput();
            }
        }
    }

    @Override
    public String deserializeString() throws DeserializeException {
        return readLine(getFieldPrompt("string"));
    }

    @Override
    public Deserializer.Map deserializeMap() {
        return new Deserializer.Map() {
            @Override
            public String nextKey(String keyHint, boolean isRequired) throws DeserializeException {
                String key = keyHint;

                if (key != null && !isRequired) {
                    pushLocation(String.format("if %s is present", keyHint), s -> String.format("if %s.%s is present", s, keyHint));
                    if (!deserializeBoolean()) {
                        popLocation();
                        return null;
                    }
                    popLocation();
                }

                if (key == null) {
                    pushLocation("key", s -> "key of " + s);

                    do {
                        key = readLine(isRequired ? "string" : "string, can be empty");
                    } while (!isRequired || key.isEmpty());

                    popLocation();
                }

                String fKey = key;
                pushLocation(fKey, s -> String.format("%s.%s", s, fKey));

                return fKey.isEmpty() ? null : fKey;
            }

            @Override
            public void nextValue(Deserializable deserializable) throws DeserializeException {
                deserializable.deserialize(CommandDeserializer.this);
                setHelp(null);
                popLocation();
            }

            @Override
            public void finish() {
            }
        };
    }

    @Override
    public Deserializer.Seq deserializeSeq() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public String formatErrorMessage(String fmt, Object... args) {
        String postfix = String.format(fmt, args);
        return getLocation() == null ? postfix : String.format("%s: %s", getLocation(), postfix);
    }

    @Override
    public void setHelp(String help) {
        this.help = help;
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

    private String getFieldPrompt(String type) {
        if (help != null) type += ", " + help;
        return getLocation() == null
                ? String.format("Enter %s: ", type)
                : String.format("Enter %s (%s): ", getLocation(), type);
    }

    private void invalidInput() {
        System.out.println("Invalid input. Please try again.");
    }

    private String readLine(String prompt) throws DeserializeException {
        try {
            String line = input.readLine(prompt);
            if (line == null) throw new DeserializeException("unexpected end of input");
            return line;
        } catch (Exception e) {
            throw new DeserializeException(e.getMessage()); // TODO
        }
    }
}
