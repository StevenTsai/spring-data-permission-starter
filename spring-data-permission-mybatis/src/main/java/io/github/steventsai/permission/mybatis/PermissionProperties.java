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
 *       header-resolver-enabled: false
 *       user-id-header: X-User-Id
 *       max-org-ids: 1000
 * }</pre>
 */
@ConfigurationProperties(prefix = "spring.data.permission")
public class PermissionProperties {

    /**
     * Whether data permission is enabled. Set to false to disable the helper and
     * all query filtering. Disabling this is a security-sensitive operation.
     */
    private boolean enabled = true;

    /**
     * Whether to register the header-based {@link PermissionArgumentResolver}.
     * <p>
     * Disabled by default because the {@code X-User-Id} header is trivially
     * spoofable. Enable it only in demos, tests, or behind a trusted gateway
     * that overwrites client-supplied values. Production applications should
     * provide a resolver backed by their JWT/session authentication instead.
     */
    private boolean headerResolverEnabled = false;

    /**
     * HTTP header name for extracting the user ID when the header resolver is enabled.
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

    /**
     * Maximum number of organization IDs allowed in one {@code IN (...)} predicate.
     * {@code 0} disables the limit. The default is {@value PermissionSqlParamAssembler#DEFAULT_MAX_ORG_IDS}.
     */
    private int maxOrgIds = PermissionSqlParamAssembler.DEFAULT_MAX_ORG_IDS;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHeaderResolverEnabled() {
        return headerResolverEnabled;
    }

    public void setHeaderResolverEnabled(boolean headerResolverEnabled) {
        this.headerResolverEnabled = headerResolverEnabled;
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

    public int getMaxOrgIds() {
        return maxOrgIds;
    }

    public void setMaxOrgIds(int maxOrgIds) {
        this.maxOrgIds = maxOrgIds;
    }
}
