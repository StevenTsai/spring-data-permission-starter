package io.github.steventsai.permission.sample.config;

import io.github.steventsai.permission.exception.AccessDeniedException;
import io.github.steventsai.permission.exception.MissingPermissionException;
import io.github.steventsai.permission.sample.exception.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for the sample application.
 * <p>
 * Access-denied and not-found responses deliberately look identical to avoid
 * leaking which record IDs exist.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, OrderNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Not Found"));
    }

    @ExceptionHandler(MissingPermissionException.class)
    public ResponseEntity<Map<String, String>> handleMissingPermission(MissingPermissionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bad Request", "message", ex.getMessage()));
    }
}
