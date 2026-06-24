package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts a {@link DataPermissionContext} into a parameter map consumable by MyBatis XML.
 * <p>
 * The generated parameters are:
 * <ul>
 *   <li>{@code authorizedOrgIds} — List of accessible organization IDs (null for ALL scope)</li>
 *   <li>{@code ownerUserId} — The owner user ID (only for SELF scope)</li>
 *   <li>{@code _orgIdColumn} — Column name for org ID filtering (configurable)</li>
 *   <li>{@code _ownerUserIdColumn} — Column name for owner user ID filtering (configurable)</li>
 * </ul>
 * <p>
 * These parameters are designed to work with the built-in
 * {@code mybatis/data-permission-fragment.xml} SQL fragment.
 */
public class PermissionSqlParamAssembler {

    private static final String DEFAULT_ORG_ID_COLUMN = "org_id";
    private static final String DEFAULT_OWNER_USER_ID_COLUMN = "owner_user_id";

    /**
     * Whitelist pattern for valid SQL column names: letters, digits, underscores,
     * optionally prefixed with a table alias (e.g. "t.org_id").
     */
    private static final Pattern VALID_COLUMN_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.]*$");

    private final String orgIdColumn;
    private final String ownerUserIdColumn;

    /**
     * Create with default column names (org_id, owner_user_id).
     */
    public PermissionSqlParamAssembler() {
        this(DEFAULT_ORG_ID_COLUMN, DEFAULT_OWNER_USER_ID_COLUMN);
    }

    /**
     * Create with custom column names.
     *
     * @param orgIdColumn       the org ID column name (e.g. "distributor_id")
     * @param ownerUserIdColumn the owner user ID column name (e.g. "creator_id")
     * @throws IllegalArgumentException if a column name contains invalid characters
     */
    public PermissionSqlParamAssembler(String orgIdColumn, String ownerUserIdColumn) {
        this.orgIdColumn = validateColumnName(orgIdColumn, DEFAULT_ORG_ID_COLUMN, "orgIdColumn");
        this.ownerUserIdColumn = validateColumnName(ownerUserIdColumn, DEFAULT_OWNER_USER_ID_COLUMN, "ownerUserIdColumn");
    }

    private static String validateColumnName(String value, String defaultValue, String propertyName) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String trimmed = value.trim();
        if (!VALID_COLUMN_NAME.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Invalid column name for " + propertyName + ": '" + trimmed + "'. "
                            + "Only letters, digits, underscores, and dots (for table aliases) are allowed.");
        }
        return trimmed;
    }

    /**
     * Assemble permission context into MyBatis-compatible parameters.
     *
     * @param context the resolved permission context
     * @return a parameter map ready to be passed to mapper methods
     */
    public Map<String, Object> assemble(DataPermissionContext context) {
        Map<String, Object> params = new HashMap<>();

        // Column name parameters (used by SQL fragment via ${_orgIdColumn})
        params.put("_orgIdColumn", orgIdColumn);
        params.put("_ownerUserIdColumn", ownerUserIdColumn);

        if (context.isAllScope()) {
            // ALL scope: no filtering
            params.put("authorizedOrgIds", null);
            params.put("ownerUserId", null);
        } else if (context.isSelfScope()) {
            // SELF scope: filter by owner user ID
            params.put("authorizedOrgIds", null);
            params.put("ownerUserId", context.getOwnerUserId());
        } else {
            // OWN_ORG / OWN_AND_CHILDREN: filter by org ID list
            params.put("authorizedOrgIds", context.getAccessibleOrgIds());
            params.put("ownerUserId", null);
        }

        return params;
    }
}
