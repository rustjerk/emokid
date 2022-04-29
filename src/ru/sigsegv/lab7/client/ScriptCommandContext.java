package ru.sigsegv.lab7.client;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ScriptCommandContext extends CommandContext {
    private final Scanner scanner;

    public ScriptCommandContext(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String readLine(String prompt) {
        try {
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                println("# " + prompt + line);
            }
            return line;
        } catch (NoSuchElementException e) {
            return null;
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
