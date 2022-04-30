package ru.sigsegv.lab7.common.serde;

@FunctionalInterface
public interface Validator<T> {
    void validate(T value);

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void validateRecord(Class<? extends Record> type, Object... fields) {
        var index = 0;
        for (var field : type.getRecordComponents()) {
            var ann = field.getAnnotation(Validate.class);
            if (ann == null) continue;

            Validator validator;

            try {
                validator = ann.value().getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            validator.validate(fields[index]);
            index += 1;
        }
    }
}
