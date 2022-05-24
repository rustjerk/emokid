package ru.sigsegv.emokid.common.util;

/**
 * ID generator
 */
public class IdGenerator {
    /**
     * Generates an ID
     *
     * @return unique id
     */
    public static long generateId() {
        var a = System.currentTimeMillis() * 0xFFFF;
        var b = System.nanoTime() & 0xFF * 0xFF;
        var c = (long) (Math.random() * 0xFF);
        return a | b | c;
    }
}
