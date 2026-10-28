package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionQueryBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link PermissionQueryBuilder}.
 * <p>
 * Assembles business parameters and permission parameters into a unified
 * parameter map that can be passed to MyBatis mapper methods.
 * <p>
 * Permission keys are reserved. A business parameter can never overwrite
 * {@code authorizedOrgIds}, {@code ownerUserId}, or any internal {@code _*}
 * key, which prevents accidental or hostile permission bypasses.
 */
public class DefaultPermissionQueryBuilder<T> implements PermissionQueryBuilder<T> {

    private final Map<String, Object> sqlParams;

    public DefaultPermissionQueryBuilder(Map<String, Object> permissionParams) {
        Objects.requireNonNull(permissionParams, "permissionParams must not be null");
        this.sqlParams = new HashMap<>(permissionParams);
    }

    @Override
    public PermissionQueryBuilder<T> param(String key, Object value) {
        if (key == null) {
            return this;
        }
        if (PermissionSqlParams.RESERVED_KEYS.contains(key)) {
            throw new IllegalArgumentException(
                    "Permission parameter '" + key + "' is reserved and cannot be overridden");
        }
        if (value != null) {
            sqlParams.put(key, value);
        }
        return this;
    }

    @Override
    public PermissionQueryBuilder<T> params(Map<String, Object> params) {
        if (params != null) {
            params.forEach(this::param);
        }
        return this;
    }

    @Override
    public Map<String, Object> getSqlParams() {
        return Collections.unmodifiableMap(new HashMap<>(sqlParams));
    }
}
