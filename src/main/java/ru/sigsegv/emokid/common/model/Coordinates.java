package ru.sigsegv.emokid.common.model;

import ru.sigsegv.emokid.common.serde.Validate;
import ru.sigsegv.emokid.common.serde.ValidationException;
import ru.sigsegv.emokid.common.serde.Validator;

import java.io.Serializable;

public record Coordinates(@Validate(XValidator.class) double x,
                          @Validate(YValidator.class) long y) implements Serializable {
    public Coordinates {
        Validator.validateRecord(Coordinates.class, x, y);
    }

    public static class XValidator implements Validator<Double> {
        @Override
        public void validate(Double x) throws ValidationException {
            if (x > 41) throw new ValidationException("cannot be above 41");
        }
    }

    public static class YValidator implements Validator<Long> {
        @Override
        public void validate(Long y) throws ValidationException {
            if (y > 107) throw new ValidationException("cannot be above 107");
        }
    }
}