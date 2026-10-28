package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.DataPermissionTable;
import io.github.steventsai.permission.StandardDataScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

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

    @Test
    void constructor_negativeMaxOrgIds_throwsException() {
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("org_id", "owner_user_id", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxOrgIds");
    }

    @Test
    void constructor_multiPartColumnName_isAccepted() {
        PermissionSqlParamAssembler assembler =
                new PermissionSqlParamAssembler("app.t.org_id", "app.t.owner_user_id");

        Map<String, Object> params = assembler.assemble(allScopeContext());

        assertThat(params).containsEntry("_orgIdColumn", "app.t.org_id");
        assertThat(params).containsEntry("_ownerUserIdColumn", "app.t.owner_user_id");
    }

    @Test
    void constructor_invalidMultiPartColumnName_throwsException() {
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("t..org_id", "owner_user_id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("1org_id", "owner_user_id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
        assertThatThrownBy(() -> new PermissionSqlParamAssembler("t.org_id.", "owner_user_id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
    }

    @Test
    void assemble_nullContext_throwsException() {
        assertThatThrownBy(() -> assembler.assemble(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("context");
    }

    @Test
    void assemble_addsScopeFlagsForAllScope() {
        Map<String, Object> params = assembler.assemble(allScopeContext());

        assertThat(params)
                .containsEntry("_scope", "ALL")
                .containsEntry("_allScope", true)
                .containsEntry("_selfScope", false)
                .containsEntry("_denyAll", false);
    }

    @Test
    void assemble_addsScopeFlagsForSelfScope() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(4L)
                .scope(StandardDataScope.SELF)
                .ownerUserId(4L)
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params)
                .containsEntry("_scope", "SELF")
                .containsEntry("_allScope", false)
                .containsEntry("_selfScope", true)
                .containsEntry("_denyAll", false)
                .containsEntry("authorizedOrgIds", null)
                .containsEntry("ownerUserId", 4L);
    }

    @Test
    void assemble_emptyOrgIds_marksDenyAll() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(Collections.emptyList())
                .build();

        Map<String, Object> params = assembler.assemble(context);

        assertThat(params)
                .containsEntry("_denyAll", true)
                .containsEntry("_allScope", false)
                .containsEntry("_selfScope", false);
        assertThat(params.get("authorizedOrgIds")).isEqualTo(Collections.emptyList());
    }

    @Test
    void assemble_exceedsDefaultMaxOrgIds_throwsException() {
        List<Long> tooManyIds = LongStream.rangeClosed(1, 1001).boxed().toList();
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(tooManyIds)
                .build();

        assertThatThrownBy(() -> assembler.assemble(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max-org-ids");
    }

    @Test
    void assemble_customMaxOrgIds_isEnforced() {
        PermissionSqlParamAssembler limited = new PermissionSqlParamAssembler("org_id", "owner_user_id", 2);
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(List.of(1L, 2L, 3L))
                .build();

        assertThatThrownBy(() -> limited.assemble(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max-org-ids=2");
    }

    @Test
    void assemble_zeroMaxOrgIds_isUnlimited() {
        PermissionSqlParamAssembler unlimited = new PermissionSqlParamAssembler("org_id", "owner_user_id", 0);
        List<Long> orgIds = LongStream.rangeClosed(1, 2000).boxed().toList();
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(orgIds)
                .build();

        assertThat(unlimited.assemble(context).get("authorizedOrgIds")).isEqualTo(orgIds);
    }

    @Test
    void assemble_usesEntityColumnOverride() {
        DataPermissionContext context = allScopeContext();

        Map<String, Object> params = assembler.assemble(context, AnnotatedEntity.class);

        assertThat(params)
                .containsEntry("_orgIdColumn", "dept_id")
                .containsEntry("_ownerUserIdColumn", "creator_id");
    }

    @Test
    void assemble_emptyAnnotationValues_fallBackToGlobalColumns() {
        PermissionSqlParamAssembler custom = new PermissionSqlParamAssembler("global_org", "global_owner");

        Map<String, Object> params = custom.assemble(allScopeContext(), DefaultAnnotatedEntity.class);

        assertThat(params)
                .containsEntry("_orgIdColumn", "global_org")
                .containsEntry("_ownerUserIdColumn", "global_owner");
    }

    @Test
    void assemble_entityWithoutAnnotation_usesGlobalColumns() {
        Map<String, Object> params = assembler.assemble(allScopeContext(), PlainEntity.class);

        assertThat(params)
                .containsEntry("_orgIdColumn", "org_id")
                .containsEntry("_ownerUserIdColumn", "owner_user_id");
    }

    @Test
    void assemble_invalidEntityColumnOverride_throwsException() {
        DataPermissionContext context = allScopeContext();

        assertThatThrownBy(() -> assembler.assemble(context, InvalidAnnotatedEntity.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid column name");
    }

    private static DataPermissionContext allScopeContext() {
        return DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();
    }

    @DataPermissionTable(orgIdColumn = "dept_id", ownerUserIdColumn = "creator_id")
    static class AnnotatedEntity {
    }

    @DataPermissionTable
    static class DefaultAnnotatedEntity {
    }

    @DataPermissionTable(orgIdColumn = "dept id")
    static class InvalidAnnotatedEntity {
    }

    static class PlainEntity {
    }
}
