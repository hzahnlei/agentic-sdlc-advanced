package io.github.hzahnlei.domain.task;

import java.util.List;

/**
 * Thrown when a Task cannot be created because the caller-supplied data
 * violates a domain invariant (e.g. blank title).
 */
public class InvalidTaskException extends RuntimeException {

    public record FieldError(String field, String reason) {}

    private final String errorCode;
    private final List<FieldError> fieldErrors;

    public InvalidTaskException(String errorCode, String message, List<FieldError> fieldErrors) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
