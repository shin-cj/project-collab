package projectcollab.common.exception;

public record FieldViolation(
        String field,
        Object rejectedValue,
        String message
) {
}
