package com.github.steventsai.permission;

import java.util.Map;

/**
 * Assembles permission parameters and optional business parameters
 * into a unified map consumable by MyBatis mapper methods.
 * <p>
 * Created via {@link DataPermissionHelper#createQuery(Class, PermissionRequest)}.
 * <p>
 * <b>This is a parameter builder, not a query executor.</b>
 * It collects key-value pairs into a parameter map. The actual SQL condition
 * assembly happens in your mapper XML, not here.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * Map<String, Object> params = permissionHelper.createQuery(Order.class, request)
 *     .param("status", "active")
 *     .param("name", "test")
 *     .getSqlParams();
 *
 * // In mapper XML, reference as params.status, params.name, etc.
 * orderMapper.selectWithScope(params);
 * }</pre>
 *
 * @param <T> the entity type
 */
public interface PermissionQueryBuilder<T> {

    /**
     * Add a business parameter to the map.
     * <p>
     * This does <b>not</b> build SQL conditions. It only puts a key-value pair
     * into the parameter map. The corresponding SQL condition (e.g.
     * {@code <if test="params.status != null">AND status = #{params.status}</if>})
     * must be written in your mapper XML.
     *
     * @param key   the parameter key (referenced in mapper XML as params.{key})
     * @param value the parameter value (null values are ignored)
     * @return this builder for chaining
     */
    PermissionQueryBuilder<T> param(String key, Object value);

    /**
     * Add multiple business parameters to the map.
     * <p>
     * Convenience method for adding several parameters at once.
     * Null values in the map are ignored.
     *
     * @param params the parameters to add (null is treated as empty map)
     * @return this builder for chaining
     */
    PermissionQueryBuilder<T> params(Map<String, Object> params);

    /**
     * Get the assembled parameter map including permission parameters and
     * any business parameters added via {@link #param} or {@link #params}.
     * <p>
     * The returned map always contains permission parameters:
     * <ul>
     *   <li>{@code authorizedOrgIds} — List of accessible organization IDs (null for ALL scope)</li>
     *   <li>{@code ownerUserId} — The owner user ID (only for SELF scope)</li>
     *   <li>{@code _orgIdColumn} — Column name for org ID filtering</li>
     *   <li>{@code _ownerUserIdColumn} — Column name for owner user ID filtering</li>
     * </ul>
     *
     * @return an unmodifiable parameter map ready to be passed to mapper methods
     */
    Map<String, Object> getSqlParams();
}
