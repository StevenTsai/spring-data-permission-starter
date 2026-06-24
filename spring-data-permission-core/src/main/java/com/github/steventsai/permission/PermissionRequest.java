package com.github.steventsai.permission;

/**
 * Permission request context. Contains the minimum information needed to resolve data permissions.
 * <p>
 * Implementations should carry at least the operator's user ID.
 * You can extend this interface to add business-specific context (e.g. tenant ID, biz type).
 */
public interface PermissionRequest {

    /**
     * The ID of the currently authenticated user.
     */
    Long operatorUserId();
}
