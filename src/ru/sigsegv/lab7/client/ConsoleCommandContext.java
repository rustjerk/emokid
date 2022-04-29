package ru.sigsegv.lab7.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleCommandContext extends CommandContext {
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

    @Override
    public void print(String line) {
        System.out.print(line);
    }

    @Override
    public void println(String line) {
        System.out.println(line);
    }
}
