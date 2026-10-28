package io.github.steventsai.permission.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user attempts to access data outside their permission scope.
 * <p>
 * The annotation makes the exception map to HTTP 403 when Spring MVC's default
 * exception handling is used. Applications may override this behavior (for
 * example, to return 404 in anti-enumeration designs).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    private final String scope;
    private final Long orgId;
    private final Long ownerId;

    public AccessDeniedException(String message) {
        super(message);
        this.scope = null;
        this.orgId = null;
        this.ownerId = null;
    }

    public AccessDeniedException(String scope, Long orgId, Long ownerId) {
        super(String.format("Access denied: scope=%s, orgId=%s, ownerId=%s", scope, orgId, ownerId));
        this.scope = scope;
        this.orgId = orgId;
        this.ownerId = ownerId;
    }

    public String getScope() {
        return scope;
    }

    public Long getOrgId() {
        return orgId;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}
