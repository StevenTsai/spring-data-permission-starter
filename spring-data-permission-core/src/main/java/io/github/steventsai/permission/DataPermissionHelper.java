package io.github.steventsai.permission;

/**
 * Core API for data permission operations.
 * <p>
 * Inject this bean in your Service layer to perform:
 * <ul>
 *   <li><b>List queries</b> — {@link #createQuery(Class, PermissionRequest)} builds a query
 *       with automatic permission filtering at the SQL layer</li>
 *   <li><b>Single-record checks</b> — {@link #checkAccess(PermissionRequest, DataPermissionTarget)}
 *       validates whether the current user can access a specific record</li>
 * </ul>
 */
public interface DataPermissionHelper {

    /**
     * Create a permission-aware query builder for list queries.
     * <p>
     * The returned builder contains permission parameters consumed by the
     * built-in {@code ScopeCondition} SQL fragment. The entity class may carry a
     * {@link DataPermissionTable} annotation to override the configured column
     * names for that entity.
     *
     * @param entityClass the entity class being queried
     * @param request     the permission request
     * @param <T>         entity type
     * @return a query builder with chainable conditions
     */
    <T> PermissionQueryBuilder<T> createQuery(Class<T> entityClass, PermissionRequest request);

    /**
     * Check whether the current user can access a specific record.
     * <p>
     * Throws {@link io.github.steventsai.permission.exception.AccessDeniedException}
     * if access is denied.
     *
     * @param request the permission request
     * @param target  the row ownership attributes
     */
    default void checkAccess(PermissionRequest request, DataPermissionTarget target) {
        checkAccess(request, target.orgId(), target.ownerId());
    }

    /**
     * Check whether the current user can access a specific record.
     * <p>
     * Prefer {@link #checkAccess(PermissionRequest, DataPermissionTarget)} to
     * avoid argument-order mistakes.
     *
     * @param request   the permission request
     * @param orgId     the organization ID the record belongs to (nullable for SELF-only records)
     * @param ownerId   the owner user ID of the record (nullable for org-scoped records)
     */
    void checkAccess(PermissionRequest request, Long orgId, Long ownerId);

    /**
     * Resolve the permission context for the given request.
     * <p>
     * Useful when you need to inspect the resolved scope or accessible org IDs directly.
     *
     * @param request the permission request
     * @return the resolved permission context
     */
    DataPermissionContext resolveContext(PermissionRequest request);
}
