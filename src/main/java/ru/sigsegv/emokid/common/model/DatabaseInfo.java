package ru.sigsegv.emokid.common.model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record DatabaseInfo(String type, int size, ZonedDateTime initializationTime) implements Serializable {
}
