package io.github.steventsai.permission;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataPermissionContextTest {

    @Test
    void builder_allScope_buildsSuccessfully() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .build();

        assertThat(context.getOperatorUserId()).isEqualTo(1L);
        assertThat(context.getScope()).isEqualTo(StandardDataScope.ALL);
        assertThat(context.getAccessibleOrgIds()).isNull();
        assertThat(context.getOwnerUserId()).isNull();
        assertThat(context.isAllScope()).isTrue();
        assertThat(context.isSelfScope()).isFalse();
    }

    @Test
    void builder_ownOrgScope_buildsWithOrgIds() {
        List<Long> orgIds = Arrays.asList(10L, 20L);

        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(orgIds)
                .build();

        assertThat(context.getScope()).isEqualTo(StandardDataScope.OWN_ORG);
        assertThat(context.getAccessibleOrgIds()).containsExactly(10L, 20L);
        assertThat(context.isAllScope()).isFalse();
        assertThat(context.isSelfScope()).isFalse();
    }

    @Test
    void builder_ownAndChildrenScope_buildsWithOrgIds() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_AND_CHILDREN)
                .accessibleOrgIds(Arrays.asList(10L, 20L, 30L))
                .build();

        assertThat(context.getScope()).isEqualTo(StandardDataScope.OWN_AND_CHILDREN);
        assertThat(context.getAccessibleOrgIds()).containsExactly(10L, 20L, 30L);
    }

    @Test
    void builder_selfScope_buildsWithOwnerUserId() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.SELF)
                .ownerUserId(1L)
                .accessibleOrgIds(Collections.emptyList())
                .build();

        assertThat(context.getScope()).isEqualTo(StandardDataScope.SELF);
        assertThat(context.getOwnerUserId()).isEqualTo(1L);
        assertThat(context.getAccessibleOrgIds()).isEmpty();
        assertThat(context.isAllScope()).isFalse();
        assertThat(context.isSelfScope()).isTrue();
    }

    @Test
    void builder_nullOperatorUserId_throwsException() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .scope(StandardDataScope.ALL)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("operatorUserId");
    }

    @Test
    void builder_nullScope_throwsException() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("scope");
    }

    @Test
    void builder_ownOrgScope_nullOrgIds_throwsException() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("accessibleOrgIds")
                .hasMessageContaining("OWN_ORG");
    }

    @Test
    void builder_ownAndChildrenScope_nullOrgIds_throwsException() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_AND_CHILDREN)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("accessibleOrgIds")
                .hasMessageContaining("OWN_AND_CHILDREN");
    }

    @Test
    void builder_selfScope_nullOwnerUserId_throwsException() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.SELF)
                .accessibleOrgIds(Collections.emptyList())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ownerUserId")
                .hasMessageContaining("SELF");
    }

    @Test
    void accessibleOrgIds_isUnmodifiable() {
        List<Long> orgIds = new java.util.ArrayList<>(Arrays.asList(10L, 20L));

        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(orgIds)
                .build();

        assertThatThrownBy(() -> context.getAccessibleOrgIds().add(30L))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void accessibleOrgIds_defensiveCopy_modifyingOriginalDoesNotAffectContext() {
        List<Long> orgIds = new java.util.ArrayList<>(Arrays.asList(10L, 20L));

        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(orgIds)
                .build();

        // Modify the original list after building
        orgIds.add(30L);
        orgIds.add(40L);

        // Context should still have the original 2 elements
        assertThat(context.getAccessibleOrgIds()).containsExactly(10L, 20L);
    }
}
