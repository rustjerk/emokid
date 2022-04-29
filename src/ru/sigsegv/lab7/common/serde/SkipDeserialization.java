package ru.sigsegv.lab7.common.serde;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotates fields, which should not be deserialized with a specified deserializer
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SkipDeserialization {
    /**
     * @return deserializer to check
     */
    Class<? extends Deserializer> deserializer();
}
