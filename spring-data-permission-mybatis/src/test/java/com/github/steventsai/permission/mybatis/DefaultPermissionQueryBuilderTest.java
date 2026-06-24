package com.github.steventsai.permission.mybatis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultPermissionQueryBuilderTest {

    private Map<String, Object> baseParams;

    @BeforeEach
    void setUp() {
        baseParams = new HashMap<>();
        baseParams.put("authorizedOrgIds", java.util.Arrays.asList(10L, 20L));
        baseParams.put("ownerUserId", null);
        baseParams.put("_orgIdColumn", "org_id");
        baseParams.put("_ownerUserIdColumn", "owner_user_id");
    }

    @Test
    void getSqlParams_returnsBaseParams() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        Map<String, Object> params = builder.getSqlParams();

        assertThat(params).containsEntry("authorizedOrgIds", java.util.Arrays.asList(10L, 20L));
        assertThat(params).containsEntry("ownerUserId", null);
        assertThat(params).containsEntry("_orgIdColumn", "org_id");
        assertThat(params).containsEntry("_ownerUserIdColumn", "owner_user_id");
    }

    @Test
    void param_addsKeyValue() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        builder.param("status", "active");

        assertThat(builder.getSqlParams()).containsEntry("status", "active");
    }

    @Test
    void param_nullKey_doesNotAdd() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        builder.param(null, "value");

        assertThat(builder.getSqlParams()).doesNotContainKey(null);
    }

    @Test
    void param_nullValue_doesNotAdd() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        builder.param("status", null);

        assertThat(builder.getSqlParams()).doesNotContainKey("status");
    }

    @Test
    void params_addsMultipleParams() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        builder.params(Map.of("status", "active", "name", "test"));

        assertThat(builder.getSqlParams()).containsEntry("status", "active");
        assertThat(builder.getSqlParams()).containsEntry("name", "test");
    }

    @Test
    void params_nullMap_doesNotAdd() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        builder.params(null);

        // Only base params should be present
        assertThat(builder.getSqlParams()).hasSize(4);
    }

    @Test
    void params_ignoresNullKeysAndValues() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);
        Map<String, Object> mixed = new HashMap<>();
        mixed.put("valid", "value");
        mixed.put(null, "nullKey");
        mixed.put("nullValue", null);

        builder.params(mixed);

        assertThat(builder.getSqlParams()).containsEntry("valid", "value");
        assertThat(builder.getSqlParams()).doesNotContainKey(null);
        assertThat(builder.getSqlParams()).doesNotContainKey("nullValue");
    }

    @Test
    void chaining_multipleParams() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        builder.param("status", "active")
                .param("name", "test")
                .params(Map.of("minAge", 18));

        assertThat(builder.getSqlParams()).containsEntry("status", "active");
        assertThat(builder.getSqlParams()).containsEntry("name", "test");
        assertThat(builder.getSqlParams()).containsEntry("minAge", 18);
    }

    @Test
    void getSqlParams_isUnmodifiable() {
        DefaultPermissionQueryBuilder<Object> builder = new DefaultPermissionQueryBuilder<>(baseParams);

        assertThatThrownBy(() -> builder.getSqlParams().put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
