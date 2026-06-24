package io.github.steventsai.permission.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a permission request cannot be resolved due to missing or invalid
 * authentication information (e.g. missing HTTP header, invalid user ID format).
 * <p>
 * This is a client-side error (HTTP 400), not a server error (HTTP 500).
 * Unlike {@link AccessDeniedException} which indicates the user was identified
 * but lacks access, this indicates the user could not be identified at all.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingPermissionException extends RuntimeException {

    public MissingPermissionException(String message) {
        super(message);
    }

    public MissingPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
