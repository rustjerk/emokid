package ru.sigsegv.emokid.common.model;

import ru.sigsegv.emokid.common.serde.NonBlankValidator;
import ru.sigsegv.emokid.common.serde.Nullable;
import ru.sigsegv.emokid.common.serde.Validate;

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
