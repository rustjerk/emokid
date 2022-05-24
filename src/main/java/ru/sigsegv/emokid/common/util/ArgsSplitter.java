package ru.sigsegv.emokid.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Arguments splitter
 */
public class ArgsSplitter {
    private static final Pattern PATTERN = Pattern.compile("((?:[^ \\\\\"]|\\\\\")+|\"(?:[^\\\\\"]|\\\\\")*\")");

    /**
     * Split arguments (shell like)
     *
     * @param str string with arguments
     * @return arguments
     */
    public static String[] splitArgs(String str) {
        List<String> result = new ArrayList<>();
        var matcher = PATTERN.matcher(str);
        while (matcher.find()) {
            var arg = matcher.group(1);
            if (arg.startsWith("\"") && arg.endsWith("\"") && arg.length() >= 2) {
                arg = arg.substring(1, arg.length() - 1);
            }

            arg = arg.replace("\\\"", "\"");
            result.add(arg);
        }

        return result.toArray(new String[0]);
    }
}
