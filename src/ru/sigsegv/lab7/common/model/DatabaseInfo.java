package ru.sigsegv.lab7.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record DatabaseInfo(String type, int size, LocalDateTime initializationTime) implements Serializable {
}
