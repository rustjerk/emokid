package ru.sigsegv.lab7.common.model;

import ru.sigsegv.lab7.common.serde.Validate;
import ru.sigsegv.lab7.common.serde.Validator;

import java.io.Serializable;

public record Coordinates(@Validate(XValidator.class) double x,
                          @Validate(YValidator.class) long y) implements Serializable {
    public Coordinates {
        Validator.validateRecord(Coordinates.class, x, y);
    }

    public static class XValidator implements Validator<Double> {
        @Override
        public void validate(Double x) {
            if (x > 41) throw new IllegalArgumentException("x cannot be above 41");
        }
    }

    public static class YValidator implements Validator<Long> {
        @Override
        public void validate(Long y) {
            if (y > 107) throw new IllegalArgumentException("y cannot be above 107");
        }
    }
}