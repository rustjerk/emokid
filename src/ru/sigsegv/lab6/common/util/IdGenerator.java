package ru.sigsegv.lab6.common.util;

/**
 * ID generator
 */
public class IdGenerator {
    /**
     * Generates an ID
     * @return unique id
     */
    public static long generateId() {
        long a = System.currentTimeMillis() * 0xFFFF;
        long b = System.nanoTime() & 0xFF * 0xFF;
        long c = (long) (Math.random() * 0xFF);
        return a | b | c;
    }
}
