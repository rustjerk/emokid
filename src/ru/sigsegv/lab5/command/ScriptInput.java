package ru.sigsegv.lab5.command;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Script command input (reads from file)
 */
public class ScriptInput implements CommandInput {
    private final Scanner scanner;

    /**
     * @param scanner scanner of the input file
     */
    public ScriptInput(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public String readLine(String prompt) {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
