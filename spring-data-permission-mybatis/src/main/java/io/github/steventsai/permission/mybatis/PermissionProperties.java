package io.github.steventsai.permission.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for spring-data-permission.
 *
 * <p><b>Example (application.yml):</b>
 * <pre>{@code
 * spring:
 *   data:
 *     permission:
 *       enabled: true
 *       user-id-header: X-User-Id
 * }</pre>
 */
@ConfigurationProperties(prefix = "spring.data.permission")
public class PermissionProperties {

    /**
     * Whether data permission is enabled. Set to false to disable all filtering.
     */
    private boolean enabled = true;

    /**
     * HTTP header name for extracting the user ID.
     */
    private String userIdHeader = "X-User-Id";

    /**
     * Column name for organization ID filtering in the ScopeCondition SQL fragment.
     * Change this if your table uses a different column name (e.g. "distributor_id").
     */
    private String orgIdColumn = "org_id";

    /**
     * Column name for owner user ID filtering in the ScopeCondition SQL fragment.
     * Change this if your table uses a different column name (e.g. "creator_id").
     */
    private String ownerUserIdColumn = "owner_user_id";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserIdHeader() {
        return userIdHeader;
    }

    public void setUserIdHeader(String userIdHeader) {
        this.userIdHeader = userIdHeader;
    }

    public String getOrgIdColumn() {
        return orgIdColumn;
    }

    public void setOrgIdColumn(String orgIdColumn) {
        this.orgIdColumn = orgIdColumn;
    }

    public String getOwnerUserIdColumn() {
        return ownerUserIdColumn;
    }

    public void setOwnerUserIdColumn(String ownerUserIdColumn) {
        this.ownerUserIdColumn = ownerUserIdColumn;
    }
}
