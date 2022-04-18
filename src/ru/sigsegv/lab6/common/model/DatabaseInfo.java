package ru.sigsegv.lab6.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DatabaseInfo implements Serializable {
    public String type;
    public int size;
    public LocalDateTime initializationTime;
}
