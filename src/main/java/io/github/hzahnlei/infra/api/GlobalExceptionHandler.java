package io.github.hzahnlei.infra.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.github.hzahnlei.domain.task.InvalidTaskException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTaskException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTask(InvalidTaskException ex) {
        List<Map<String, String>> details = ex.getFieldErrors().stream()
                .map(e -> Map.of("field", e.field(), "reason", e.reason()))
                .toList();
        return ResponseEntity.status(400)
                .body(buildEnvelope(ex.getErrorCode(), ex.getMessage(), details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> details = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String fieldPath = extractConstraintPath(cv.getPropertyPath());
                    if (fieldPath.isEmpty()) return null;
                    return Map.of("field", fieldPath, "reason", cv.getMessage());
                })
                .filter(Objects::nonNull)
                .toList();
        return ResponseEntity.status(400)
                .body(buildEnvelope("INVALID_INPUT", "One or more input values are invalid.", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife
                && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            return enumValidationFailed(ife.getPath(), ife.getTargetType());
        }
        // @JsonCreator that throws IllegalArgumentException is wrapped as ValueInstantiationException
        if (cause instanceof ValueInstantiationException vie
                && vie.getType() != null
                && vie.getType().getRawClass().isEnum()) {
            return enumValidationFailed(vie.getPath(), vie.getType().getRawClass());
        }
        return ResponseEntity.status(400)
                .body(buildEnvelope("INVALID_INPUT",
                        "Request body is not valid JSON or is missing required fields.", List.of()));
    }

    private ResponseEntity<Map<String, Object>> enumValidationFailed(
            List<com.fasterxml.jackson.databind.JsonMappingException.Reference> path,
            Class<?> enumType) {
        String fieldPath = buildFieldPath(path);
        String allowedValues = Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        List<Map<String, String>> details = fieldPath.isEmpty()
                ? List.of()
                : List.of(Map.of("field", fieldPath, "reason", "must be one of " + allowedValues));
        return ResponseEntity.status(422)
                .body(buildEnvelope("VALIDATION_FAILED",
                        "One or more task records contain invalid field values.", details));
    }

    private String buildFieldPath(List<com.fasterxml.jackson.databind.JsonMappingException.Reference> path) {
        StringBuilder sb = new StringBuilder();
        for (var ref : path) {
            if (ref.getIndex() >= 0) {
                sb.append("[").append(ref.getIndex()).append("]");
            } else if (ref.getFieldName() != null) {
                if (!sb.isEmpty()) sb.append(".");
                sb.append(ref.getFieldName());
            }
        }
        return sb.toString();
    }

    private String extractConstraintPath(Path path) {
        // Path for method constraints: methodName.paramName[index].fieldName
        // Skip the first 2 nodes (method name and parameter name).
        // A PropertyNode inside a List carries both getIndex() and getName()
        // in a single node, so both must be appended independently.
        StringBuilder sb = new StringBuilder();
        int nodeIndex = 0;
        for (Path.Node node : path) {
            if (nodeIndex++ < 2) continue;
            if (node.getIndex() != null) {
                sb.append("[").append(node.getIndex()).append("]");
            }
            String name = node.getName();
            if (name != null && !name.startsWith("<")) {
                if (!sb.isEmpty()) sb.append(".");
                sb.append(name);
            }
        }
        return sb.toString();
    }

    private Map<String, Object> buildEnvelope(String code, String message,
                                               List<Map<String, String>> details) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        if (!details.isEmpty()) {
            error.put("details", details);
        }
        return Map.of("error", error);
    }
}
