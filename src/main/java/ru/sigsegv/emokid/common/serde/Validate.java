package ru.sigsegv.emokid.common.serde;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
    Class<? extends Validator<?>> value();
}
