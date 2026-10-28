package io.github.steventsai.permission.mybatis;

import java.util.Set;

/**
 * Reserved parameter keys used by the built-in {@code ScopeCondition} SQL fragment.
 * <p>
 * These keys are owned by the framework. Business code must never be able to
 * overwrite them, otherwise a caller could silently remove a permission filter.
 */
public final class PermissionSqlParams {

    /** List of accessible organization IDs. */
    public static final String AUTHORIZED_ORG_IDS = "authorizedOrgIds";

    /** Owner user ID used by SELF scope. */
    public static final String OWNER_USER_ID = "ownerUserId";

    /** Validated organization ID column name. */
    public static final String ORG_ID_COLUMN = "_orgIdColumn";

    /** Validated owner user ID column name. */
    public static final String OWNER_USER_ID_COLUMN = "_ownerUserIdColumn";

    /** Scope code, useful for logging and debugging. */
    public static final String SCOPE = "_scope";

    /** Whether the scope is unrestricted. */
    public static final String ALL_SCOPE = "_allScope";

    /** Whether the scope is self-owned only. */
    public static final String SELF_SCOPE = "_selfScope";

    /** Whether the query must deny all rows. */
    public static final String DENY_ALL = "_denyAll";

    /**
     * All keys that business parameters are forbidden to override.
     */
    public static final Set<String> RESERVED_KEYS = Set.of(
            AUTHORIZED_ORG_IDS,
            OWNER_USER_ID,
            ORG_ID_COLUMN,
            OWNER_USER_ID_COLUMN,
            SCOPE,
            ALL_SCOPE,
            SELF_SCOPE,
            DENY_ALL
    );

    private PermissionSqlParams() {
    }
}
