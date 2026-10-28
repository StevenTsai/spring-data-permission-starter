package io.github.steventsai.permission;

/**
 * The ownership attributes of a single row being checked for access.
 * <p>
 * Using a named record avoids the common mistake of swapping the two
 * {@code Long} arguments of the legacy
 * {@link DataPermissionHelper#checkAccess(PermissionRequest, Long, Long)}
 * overload.
 *
 * @param orgId   the organization ID of the row, or {@code null} when not applicable
 * @param ownerId the owner user ID of the row, or {@code null} when not applicable
 */
public record DataPermissionTarget(Long orgId, Long ownerId) {

    /**
     * Create a target from its ownership attributes.
     *
     * @param orgId   the organization ID
     * @param ownerId the owner user ID
     * @return a target record
     */
    public static DataPermissionTarget of(Long orgId, Long ownerId) {
        return new DataPermissionTarget(orgId, ownerId);
    }
}
