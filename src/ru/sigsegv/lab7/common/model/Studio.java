package ru.sigsegv.lab7.common.model;

import ru.sigsegv.lab7.common.serde.NonBlankValidator;
import ru.sigsegv.lab7.common.serde.Nullable;
import ru.sigsegv.lab7.common.serde.Validate;

import java.io.Serializable;

/**
 * Studio data record
 */
public record Studio(@Nullable String name,
                     @Validate(NonBlankValidator.class) String address) implements Comparable<Studio>, Serializable {
    @Override
    public int compareTo(Studio o) {
        if (name == null || o.name == null) return 0;
        return name.toLowerCase().compareTo(o.name.toLowerCase());
    }
}
