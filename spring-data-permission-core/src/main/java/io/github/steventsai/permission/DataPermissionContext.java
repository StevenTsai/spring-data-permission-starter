package io.github.steventsai.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable data permission context resolved from a {@link PermissionRequest}.
 * <p>
 * This object is created by {@link PermissionResolver#resolve(PermissionRequest)}
 * and consumed by {@link DataPermissionHelper} for filtering and access checks.
 */
public final class DataPermissionContext {

    private final Long operatorUserId;
    private final StandardDataScope scope;
    private final List<Long> accessibleOrgIds;
    private final Long ownerUserId;

    private DataPermissionContext(Builder builder) {
        this.operatorUserId = builder.operatorUserId;
        this.scope = builder.scope;
        this.accessibleOrgIds = builder.accessibleOrgIds == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(builder.accessibleOrgIds));
        this.ownerUserId = builder.ownerUserId;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public StandardDataScope getScope() {
        return scope;
    }

    /**
     * Accessible organization IDs.
     * <ul>
     *   <li>{@code null} — ALL scope, no restriction</li>
     *   <li>Non-empty list — OWN_ORG or OWN_AND_CHILDREN scope</li>
     *   <li>Empty list — SELF scope (use ownerUserId instead)</li>
     * </ul>
     */
    public List<Long> getAccessibleOrgIds() {
        return accessibleOrgIds;
    }

    /**
     * The owner user ID for SELF scope filtering.
     * Only meaningful when scope is SELF.
     */
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public boolean isAllScope() {
        return scope == StandardDataScope.ALL;
    }

    public boolean isSelfScope() {
        return scope == StandardDataScope.SELF;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long operatorUserId;
        private StandardDataScope scope;
        private List<Long> accessibleOrgIds;
        private Long ownerUserId;

        private Builder() {}

        public Builder operatorUserId(Long operatorUserId) {
            this.operatorUserId = operatorUserId;
            return this;
        }

        public Builder scope(StandardDataScope scope) {
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
            // Scope-specific invariant checks
            if (scope == StandardDataScope.OWN_ORG || scope == StandardDataScope.OWN_AND_CHILDREN) {
                if (accessibleOrgIds == null) {
                    throw new IllegalStateException(
                            "accessibleOrgIds must not be null for scope " + scope.getCode()
                                    + ". PermissionResolver must resolve accessible organization IDs.");
                }
            }
            if (scope == StandardDataScope.SELF && ownerUserId == null) {
                throw new IllegalStateException(
                        "ownerUserId must not be null for scope SELF. "
                                + "PermissionResolver must resolve the owner user ID.");
            }
            return new DataPermissionContext(this);
        }
    }
}
