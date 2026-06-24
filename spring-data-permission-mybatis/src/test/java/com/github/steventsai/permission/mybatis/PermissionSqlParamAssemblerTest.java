package com.github.steventsai.permission.mybatis;

import com.github.steventsai.permission.DataPermissionContext;
import com.github.steventsai.permission.StandardDataScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PermissionSqlParamAssemblerTest {

    private PermissionSqlParamAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new PermissionSqlParamAssembler();
    }

    @Test
    void assemble_allScope_bothParamsNull() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params).containsEntry("authorizedOrgIds", null);
        assertThat(params).containsEntry("ownerUserId", null);
    }

    @Test
    void assemble_ownOrgScope_authorizedOrgIdsPopulated() {
        List<Long> orgIds = Arrays.asList(10L, 20L, 30L);
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(orgIds)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params.get("authorizedOrgIds")).isEqualTo(orgIds);
        assertThat(params).containsEntry("ownerUserId", null);
    }

    @Test
    void assemble_ownAndChildrenScope_authorizedOrgIdsPopulated() {
        List<Long> orgIds = Arrays.asList(10L, 20L, 30L, 40L);
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_AND_CHILDREN)
                .accessibleOrgIds(orgIds)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params.get("authorizedOrgIds")).isEqualTo(orgIds);
        assertThat(params).containsEntry("ownerUserId", null);
    }

    @Test
    void assemble_selfScope_ownerUserIdPopulated() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(4L)
                .scope(StandardDataScope.SELF)
                .ownerUserId(4L)
                .accessibleOrgIds(java.util.Collections.emptyList())
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params).containsEntry("authorizedOrgIds", null);
        assertThat(params).containsEntry("ownerUserId", 4L);
    }

    @Test
    void assemble_defaultColumnNames() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params).containsEntry("_orgIdColumn", "org_id");
        assertThat(params).containsEntry("_ownerUserIdColumn", "owner_user_id");
    }

    @Test
    void assemble_customColumnNames() {
        PermissionSqlParamAssembler customAssembler = new PermissionSqlParamAssembler("distributor_id", "creator_id");

        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        Map<String, Object> params = customAssembler.assemble(context);

        assertThat(params).containsEntry("_orgIdColumn", "distributor_id");
        assertThat(params).containsEntry("_ownerUserIdColumn", "creator_id");
    }

    @Test
    void assemble_nullColumnNames_usesDefaults() {
        PermissionSqlParamAssembler assembler = new PermissionSqlParamAssembler(null, null);

        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params).containsEntry("_orgIdColumn", "org_id");
        assertThat(params).containsEntry("_ownerUserIdColumn", "owner_user_id");
    }

    @Test
    void assemble_returnsNewMapEachTime() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        Map<String, Object> params1 = assembler.assemble(context);
        Map<String, Object> params2 = assembler.assemble(context);

        assertThat(params1).isNotSameAs(params2);
        assertThat(params1).isEqualTo(params2);
    }

    @Test
    void constructor_validColumnNameWithDot_isAccepted() {
        PermissionSqlParamAssembler assembler = new PermissionSqlParamAssembler("t.org_id", "t.owner_user_id");

        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params).containsEntry("_orgIdColumn", "t.org_id");
        assertThat(params).containsEntry("_ownerUserIdColumn", "t.owner_user_id");
    }

    @Test
    void constructor_sqlInjectionAttempt_throwsException() {
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("org_id; DROP TABLE users", "owner_user_id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
    }

    @Test
    void constructor_specialChars_throwsException() {
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("org'id", "owner_user_id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
    }

    @Test
    void constructor_spaces_throwsException() {
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("org id", "owner_user_id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
    }
}
