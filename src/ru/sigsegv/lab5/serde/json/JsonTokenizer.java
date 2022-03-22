package ru.sigsegv.lab5.serde.json;

import ru.sigsegv.lab5.serde.DeserializeException;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * JSON tokenizer
 */
public class JsonTokenizer {
    private static final Pattern WHITESPACE = Pattern.compile("[ \t\r\n]");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("null|true|false|[\\[\\]{},:]|-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?|\"(?:\\\\\"|[^\"])*\"");

    private final Scanner scanner;
    private String string = null;
    private double number = 0;

    /**
     * @param scanner scanner of the input
     */
    public JsonTokenizer(Scanner scanner) {
        this.scanner = scanner;
        scanner.useDelimiter("");
    }

    /**
     * Reads the next token from the input.
     * @return the next token
     * @throws DeserializeException if the input is incorrect
     */
    public JsonToken nextToken() throws DeserializeException {
        if (scanner.hasNext(WHITESPACE))
            scanner.skip(WHITESPACE);
        String match = scanner.findWithinHorizon(TOKEN_PATTERN, 0);

        if (match == null && scanner.hasNext())
            throw new DeserializeException("syntax error: trailing characters");
        if (match == null)
            return null;

        switch (match) {
            case "null":
                return JsonToken.NULL;
            case "true":
                return JsonToken.TRUE;
            case "false":
                return JsonToken.FALSE;
            case "[":
                return JsonToken.L_BRACKET;
            case "]":
                return JsonToken.R_BRACKET;
            case "{":
                return JsonToken.L_BRACE;
            case "}":
                return JsonToken.R_BRACE;
            case ",":
                return JsonToken.COMMA;
            case ":":
                return JsonToken.COLON;
            default:
                if (match.startsWith("'") || match.startsWith("\"")) {
                    string = unescapeString(match);
                    return JsonToken.STRING;
                }

                number = Double.parseDouble(match);
                return JsonToken.NUMBER;
        }
    }

    /**
     * Returns the string payload of the previous string token
     * @return the string payload
     */
    public String getString() {
        return string;
    }

    /**
     * Returns the number payload of the previous number token
     * @return the number payload
     */
    public double getNumber() {
        return number;
    }

    private static String unescapeString(String input) throws DeserializeException {
        input = input.substring(1, input.length() - 1);
        StringBuilder result = new StringBuilder(input.length());

        boolean isEscaped = false;
        int unicodeLen = -1;
        char unicodeChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                isEscaped = true;
                continue;
            }

            if (isEscaped && unicodeLen == -1) {
                switch (c) {
                    case 'b':
                        c = '\b';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'u':
                        unicodeLen = 0;
                        unicodeChar = 0;
                        continue;
                    default:
                        // Do nothing
                }
            } else if (unicodeLen >= 0) {
                int digit = Character.digit(c, 16);
                if (digit < 0)
                    throw new DeserializeException("invalid unicode escape");

                unicodeChar *= 16;
                unicodeChar += digit;
                unicodeLen += 1;

                if (unicodeLen == 4) {
                    unicodeLen = -1;
                    c = unicodeChar;
                } else
                    continue;
            }

            result.append(c);
            isEscaped = false;
        }

        return result.toString();
    }
}
