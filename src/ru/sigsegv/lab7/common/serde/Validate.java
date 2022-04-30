package ru.sigsegv.lab7.common.serde;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
    Class<? extends Validator<?>> value();
}
