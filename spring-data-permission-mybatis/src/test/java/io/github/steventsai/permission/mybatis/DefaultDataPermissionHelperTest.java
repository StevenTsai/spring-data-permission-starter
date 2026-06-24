package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.PermissionQueryBuilder;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.PermissionResolver;
import io.github.steventsai.permission.StandardDataScope;
import io.github.steventsai.permission.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultDataPermissionHelperTest {

    private PermissionResolver permissionResolver;
    private PermissionSqlParamAssembler paramAssembler;
    private DefaultDataPermissionHelper helper;

    @BeforeEach
    void setUp() {
        permissionResolver = mock(PermissionResolver.class);
        paramAssembler = new PermissionSqlParamAssembler();
        helper = new DefaultDataPermissionHelper(permissionResolver, paramAssembler);
    }

    // --- checkAccess tests ---

    @Test
    void checkAccess_allScope_alwaysPasses() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.ALL)
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        // Should not throw
        helper.checkAccess(request, 999L, 999L);
    }

    @Test
    void checkAccess_ownOrgScope_matchingOrg_passes() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.OWN_ORG)
                        .accessibleOrgIds(Arrays.asList(10L, 20L))
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        helper.checkAccess(request, 10L, null);
    }

    @Test
    void checkAccess_ownOrgScope_nonMatchingOrg_throwsAccessDenied() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.OWN_ORG)
                        .accessibleOrgIds(Arrays.asList(10L, 20L))
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        assertThatThrownBy(() -> helper.checkAccess(request, 99L, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkAccess_ownOrgScope_nullOrgId_throwsAccessDenied() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.OWN_ORG)
                        .accessibleOrgIds(Arrays.asList(10L, 20L))
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        assertThatThrownBy(() -> helper.checkAccess(request, null, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkAccess_ownOrgScope_emptyOrgIds_throwsAccessDenied() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.OWN_ORG)
                        .accessibleOrgIds(Collections.emptyList())
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        assertThatThrownBy(() -> helper.checkAccess(request, 10L, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkAccess_selfScope_matchingOwner_passes() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(4L)
                        .scope(StandardDataScope.SELF)
                        .ownerUserId(4L)
                        .accessibleOrgIds(Collections.emptyList())
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(4L);
        helper.checkAccess(request, null, 4L);
    }

    @Test
    void checkAccess_selfScope_nonMatchingOwner_throwsAccessDenied() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(4L)
                        .scope(StandardDataScope.SELF)
                        .ownerUserId(4L)
                        .accessibleOrgIds(Collections.emptyList())
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(4L);
        assertThatThrownBy(() -> helper.checkAccess(request, null, 5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void checkAccess_selfScope_nullOwner_throwsAccessDenied() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(4L)
                        .scope(StandardDataScope.SELF)
                        .ownerUserId(4L)
                        .accessibleOrgIds(Collections.emptyList())
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(4L);
        assertThatThrownBy(() -> helper.checkAccess(request, null, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    // --- createQuery tests ---

    @Test
    void createQuery_allScope_returnsBuilderWithNullParams() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.ALL)
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        PermissionQueryBuilder<Object> query = helper.createQuery(Object.class, request);

        Map<String, Object> params = query.getSqlParams();
        assertThat(params).containsEntry("authorizedOrgIds", null);
        assertThat(params).containsEntry("ownerUserId", null);
    }

    @Test
    void createQuery_ownOrgScope_returnsBuilderWithOrgIds() {
        when(permissionResolver.resolve(any())).thenReturn(
                DataPermissionContext.builder()
                        .operatorUserId(1L)
                        .scope(StandardDataScope.OWN_ORG)
                        .accessibleOrgIds(Arrays.asList(10L, 20L))
                        .build());

        PermissionRequest request = new DefaultPermissionRequest(1L);
        PermissionQueryBuilder<Object> query = helper.createQuery(Object.class, request);

        Map<String, Object> params = query.getSqlParams();
        assertThat(params.get("authorizedOrgIds")).isEqualTo(Arrays.asList(10L, 20L));
        assertThat(params).containsEntry("ownerUserId", null);
    }

    // --- resolveContext tests ---

    @Test
    void resolveContext_delegatesToResolver() {
        DataPermissionContext expected = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();
        when(permissionResolver.resolve(any())).thenReturn(expected);

        PermissionRequest request = new DefaultPermissionRequest(1L);
        DataPermissionContext result = helper.resolveContext(request);

        assertThat(result).isSameAs(expected);
    }
}
