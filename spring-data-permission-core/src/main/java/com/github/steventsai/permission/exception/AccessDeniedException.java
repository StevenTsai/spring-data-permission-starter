package com.github.steventsai.permission.exception;

/**
 * Thrown when a user attempts to access data outside their permission scope.
 */
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
