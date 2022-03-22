package ru.sigsegv.lab5.command;

/**
 * Command input
 */
public interface CommandInput {
    /**
     * Prints a prompt (in a console environment only) and then reads a single string.
     * @param prompt a string to be printed before the input (example: "&gt; ")
     * @return line read, or null if the end of file has been reached
     */
    String readLine(String prompt);
}
