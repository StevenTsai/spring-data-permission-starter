package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.DataPermissionTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Converts a {@link DataPermissionContext} into a parameter map consumable by MyBatis XML.
 * <p>
 * The generated parameters are:
 * <ul>
 *   <li>{@code authorizedOrgIds} — List of accessible organization IDs (null for ALL/SELF)</li>
 *   <li>{@code ownerUserId} — The owner user ID (only for SELF scope)</li>
 *   <li>{@code _orgIdColumn} — Column name for org ID filtering (configurable)</li>
 *   <li>{@code _ownerUserIdColumn} — Column name for owner user ID filtering (configurable)</li>
 *   <li>{@code _scope}, {@code _allScope}, {@code _selfScope}, {@code _denyAll} — scope flags</li>
 * </ul>
 * <p>
 * These parameters are designed to work with the built-in
 * {@code mybatis/data-permission-fragment.xml} SQL fragment.
 */
public class PermissionSqlParamAssembler {

    /**
     * Default upper bound for the number of organization IDs included in an
     * {@code IN (...)} predicate. Set {@code spring.data.permission.max-org-ids}
     * to {@code 0} for no limit.
     */
    public static final int DEFAULT_MAX_ORG_IDS = 1000;

    private static final String DEFAULT_ORG_ID_COLUMN = "org_id";
    private static final String DEFAULT_OWNER_USER_ID_COLUMN = "owner_user_id";

    /**
     * Whitelist pattern for valid SQL column names. Supports one or more
     * dot-separated identifiers, e.g. {@code org_id}, {@code t.org_id}, or
     * {@code app.t.org_id}.
     */
    private static final Pattern VALID_COLUMN_NAME =
            Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");

    private final String orgIdColumn;
    private final String ownerUserIdColumn;
    private final int maxOrgIds;

    /**
     * Create with default column names ({@code org_id}, {@code owner_user_id})
     * and the default organization ID limit.
     */
    public PermissionSqlParamAssembler() {
        this(DEFAULT_ORG_ID_COLUMN, DEFAULT_OWNER_USER_ID_COLUMN);
    }

    /**
     * Create with custom column names and the default organization ID limit.
     *
     * @param orgIdColumn       the org ID column name (e.g. "distributor_id")
     * @param ownerUserIdColumn the owner user ID column name (e.g. "creator_id")
     * @throws IllegalArgumentException if a column name contains invalid characters
     */
    public PermissionSqlParamAssembler(String orgIdColumn, String ownerUserIdColumn) {
        this(orgIdColumn, ownerUserIdColumn, DEFAULT_MAX_ORG_IDS);
    }

    /**
     * Create with custom column names and a maximum organization ID count.
     *
     * @param orgIdColumn       the org ID column name
     * @param ownerUserIdColumn the owner user ID column name
     * @param maxOrgIds         maximum IDs in an IN predicate; {@code 0} means unlimited
     * @throws IllegalArgumentException if a column name is invalid or {@code maxOrgIds} is negative
     */
    public PermissionSqlParamAssembler(String orgIdColumn, String ownerUserIdColumn, int maxOrgIds) {
        if (maxOrgIds < 0) {
            throw new IllegalArgumentException("maxOrgIds must not be negative");
        }
        this.orgIdColumn = validateColumnName(orgIdColumn, DEFAULT_ORG_ID_COLUMN, "orgIdColumn");
        this.ownerUserIdColumn = validateColumnName(ownerUserIdColumn, DEFAULT_OWNER_USER_ID_COLUMN, "ownerUserIdColumn");
        this.maxOrgIds = maxOrgIds;
    }

    private static String validateColumnName(String value, String defaultValue, String propertyName) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String trimmed = value.trim();
        if (!VALID_COLUMN_NAME.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Invalid column name for " + propertyName + ": '" + trimmed + "'. "
                            + "Only letters, digits, underscores, and dot-separated identifiers are allowed.");
        }
        return trimmed;
    }

    /**
     * Assemble permission context using the globally configured columns.
     *
     * @param context the resolved permission context
     * @return a parameter map ready to be passed to mapper methods
     */
    public Map<String, Object> assemble(DataPermissionContext context) {
        return assemble(context, null);
    }

    /**
     * Assemble permission context, optionally applying a {@link DataPermissionTable}
     * override from the queried entity type.
     *
     * @param context     the resolved permission context
     * @param entityClass the entity type being queried, or {@code null}
     * @return a parameter map ready to be passed to mapper methods
     */
    public Map<String, Object> assemble(DataPermissionContext context, Class<?> entityClass) {
        Objects.requireNonNull(context, "context must not be null");

        String resolvedOrgIdColumn = orgIdColumn;
        String resolvedOwnerUserIdColumn = ownerUserIdColumn;
        if (entityClass != null) {
            DataPermissionTable table = entityClass.getAnnotation(DataPermissionTable.class);
            if (table != null) {
                resolvedOrgIdColumn = validateColumnName(
                        table.orgIdColumn(), resolvedOrgIdColumn, "orgIdColumn");
                resolvedOwnerUserIdColumn = validateColumnName(
                        table.ownerUserIdColumn(), resolvedOwnerUserIdColumn, "ownerUserIdColumn");
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put(PermissionSqlParams.ORG_ID_COLUMN, resolvedOrgIdColumn);
        params.put(PermissionSqlParams.OWNER_USER_ID_COLUMN, resolvedOwnerUserIdColumn);
        params.put(PermissionSqlParams.SCOPE, context.getScope().getCode());
        params.put(PermissionSqlParams.ALL_SCOPE, context.isAllScope());
        params.put(PermissionSqlParams.SELF_SCOPE, context.isSelfScope());
        params.put(PermissionSqlParams.DENY_ALL, context.isDenyAll());

        if (context.isAllScope()) {
            params.put(PermissionSqlParams.AUTHORIZED_ORG_IDS, null);
            params.put(PermissionSqlParams.OWNER_USER_ID, null);
        } else if (context.isSelfScope()) {
            params.put(PermissionSqlParams.AUTHORIZED_ORG_IDS, null);
            params.put(PermissionSqlParams.OWNER_USER_ID, context.getOwnerUserId());
        } else {
            List<Long> orgIds = context.getAccessibleOrgIds();
            if (maxOrgIds > 0 && orgIds.size() > maxOrgIds) {
                throw new IllegalArgumentException(
                        "accessibleOrgIds contains " + orgIds.size() + " entries, which exceeds "
                                + "spring.data.permission.max-org-ids=" + maxOrgIds
                                + ". Use a scoped subquery or raise the limit explicitly.");
            }
            params.put(PermissionSqlParams.AUTHORIZED_ORG_IDS, orgIds);
            params.put(PermissionSqlParams.OWNER_USER_ID, null);
        }

        return params;
    }
}
