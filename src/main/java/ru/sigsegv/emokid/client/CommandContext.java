package ru.sigsegv.emokid.client;

public abstract class CommandContext {
    private boolean isRunning = true;

    public String defaultPrompt() {
        return "> ";
    }

    public abstract String readLine(String prompt);

    public abstract void print(String line);

    public abstract void println(String line);

    public void flush() {
    }

    public void printf(String fmt, Object... args) {
        print(String.format(fmt, args));
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
