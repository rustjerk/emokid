package ru.sigsegv.lab5.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Console command input (reads from standard input)
 */
public class ConsoleInput implements CommandInput {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public String readLine(String prompt) {
        try {
            System.out.print(prompt);
            return reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
