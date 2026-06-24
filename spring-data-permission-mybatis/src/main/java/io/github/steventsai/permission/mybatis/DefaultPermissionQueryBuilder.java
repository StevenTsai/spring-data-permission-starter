package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionQueryBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link PermissionQueryBuilder}.
 * <p>
 * Assembles business parameters and permission parameters into a unified
 * parameter map that can be passed to MyBatis mapper methods.
 */
public class DefaultPermissionQueryBuilder<T> implements PermissionQueryBuilder<T> {

    private final Map<String, Object> sqlParams;

    public DefaultPermissionQueryBuilder(Map<String, Object> permissionParams) {
        this.sqlParams = new HashMap<>(permissionParams);
    }

    @Override
    public PermissionQueryBuilder<T> param(String key, Object value) {
        if (key != null && value != null) {
            sqlParams.put(key, value);
        }
        return this;
    }

    @Override
    public PermissionQueryBuilder<T> params(Map<String, Object> params) {
        if (params != null) {
            params.forEach((k, v) -> {
                if (k != null && v != null) {
                    sqlParams.put(k, v);
                }
            });
        }
        return this;
    }

    @Override
    public Map<String, Object> getSqlParams() {
        return Collections.unmodifiableMap(sqlParams);
    }
}
