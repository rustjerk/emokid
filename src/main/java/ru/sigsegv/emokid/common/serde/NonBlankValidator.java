package ru.sigsegv.emokid.common.serde;

public class NonBlankValidator implements Validator<String> {
    @Override
    public void validate(String value) throws ValidationException {
        if (value == null || value.isBlank()) throw new ValidationException("cannot be empty");
    }
}
