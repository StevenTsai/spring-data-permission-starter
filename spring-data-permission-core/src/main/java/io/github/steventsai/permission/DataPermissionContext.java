package io.github.steventsai.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Immutable data permission context resolved from a {@link PermissionRequest}.
 * <p>
 * This object is created by {@link PermissionResolver#resolve(PermissionRequest)}
 * and consumed by {@link DataPermissionHelper} for filtering and access checks.
 * <p>
 * An organization-scoped context with an empty {@link #getAccessibleOrgIds()}
 * list is a valid <strong>deny-all</strong> context. Queries generated from it
 * contain a {@code 1=0} predicate instead of silently returning every row.
 */
public final class DataPermissionContext {

    private final Long operatorUserId;
    private final DataScope scope;
    private final List<Long> accessibleOrgIds;
    private final Long ownerUserId;

    private DataPermissionContext(Builder builder) {
        this.operatorUserId = builder.operatorUserId;
        this.scope = builder.scope;
        if (builder.scope.isAll()) {
            this.accessibleOrgIds = null;
            this.ownerUserId = null;
        } else if (builder.scope.isSelf()) {
            this.accessibleOrgIds = builder.accessibleOrgIds == null
                    ? Collections.emptyList()
                    : immutableCopy(builder.accessibleOrgIds);
            this.ownerUserId = builder.ownerUserId;
        } else {
            this.accessibleOrgIds = immutableCopy(builder.accessibleOrgIds);
            this.ownerUserId = null;
        }
    }

    private static List<Long> immutableCopy(List<Long> source) {
        return Collections.unmodifiableList(new ArrayList<>(new LinkedHashSet<>(source)));
    }

    private static boolean containsNull(List<Long> values) {
        for (Long value : values) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    /**
     * The resolved data scope.
     *
     * @return the non-null scope
     */
    public DataScope getScope() {
        return scope;
    }

    /**
     * Accessible organization IDs.
     * <ul>
     *   <li>{@code null} — ALL scope, no restriction</li>
     *   <li>Non-empty list — organization-scoped access</li>
     *   <li>Empty list — organization-scoped access with no accessible orgs
     *       (deny-all), or SELF scope</li>
     * </ul>
     *
     * @return an immutable list, or {@code null} for ALL scope
     */
    public List<Long> getAccessibleOrgIds() {
        return accessibleOrgIds;
    }

    /**
     * The owner user ID for SELF scope filtering.
     * Only meaningful when the scope {@link DataScope#isSelf() is SELF}.
     *
     * @return the owner user ID, or {@code null} for non-SELF scopes
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Whether the resolved scope grants unrestricted access.
     *
     * @return {@code true} for ALL (or a custom all) scope
     */
    public boolean isAllScope() {
        return scope.isAll();
    }

    /**
     * Whether the resolved scope restricts rows to the operator.
     *
     * @return {@code true} for SELF (or a custom self) scope
     */
    public boolean isSelfScope() {
        return scope.isSelf();
    }

    /**
     * Whether this context must deny access to every row.
     * <p>
     * This is deliberately fail-closed: an organization-scoped context with
     * no accessible organizations is different from an ALL context.
     *
     * @return {@code true} when no row may be returned
     */
    public boolean isDenyAll() {
        return !isAllScope() && !isSelfScope()
                && (accessibleOrgIds == null || accessibleOrgIds.isEmpty());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "DataPermissionContext{"
                + "operatorUserId=" + operatorUserId
                + ", scope=" + scope.getCode()
                + ", accessibleOrgIds=" + accessibleOrgIds
                + ", ownerUserId=" + ownerUserId
                + '}';
    }

    public static final class Builder {
        private Long operatorUserId;
        private DataScope scope;
        private List<Long> accessibleOrgIds;
        private Long ownerUserId;

        private Builder() {
        }

        public Builder operatorUserId(Long operatorUserId) {
            this.operatorUserId = operatorUserId;
            return this;
        }

        /**
         * Set the resolved scope.
         *
         * @param scope a standard or custom data scope
         * @return this builder
         */
        public Builder scope(DataScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder accessibleOrgIds(List<Long> accessibleOrgIds) {
            this.accessibleOrgIds = accessibleOrgIds;
            return this;
        }

        public Builder ownerUserId(Long ownerUserId) {
            this.ownerUserId = ownerUserId;
            return this;
        }

        public DataPermissionContext build() {
            if (operatorUserId == null) {
                throw new IllegalStateException("operatorUserId must not be null");
            }
            if (scope == null) {
                throw new IllegalStateException("scope must not be null");
            }
            String scopeCode = scope.getCode();
            if (scopeCode == null || scopeCode.isBlank()) {
                throw new IllegalStateException("scope.getCode() must not be blank");
            }

            if (scope.isSelf() && ownerUserId == null) {
                throw new IllegalStateException(
                        "ownerUserId must not be null for scope " + scopeCode
                                + ". PermissionResolver must resolve the owner user ID.");
            }
            if (!scope.isAll() && !scope.isSelf()) {
                if (accessibleOrgIds == null) {
                    throw new IllegalStateException(
                            "accessibleOrgIds must not be null for scope " + scopeCode
                                    + ". PermissionResolver must resolve accessible organization IDs. "
                                    + "Use an empty list for deny-all.");
                }
                if (containsNull(accessibleOrgIds)) {
                    throw new IllegalStateException(
                            "accessibleOrgIds must not contain null elements for scope " + scopeCode);
                }
            }
            return new DataPermissionContext(this);
        }
    }
}
