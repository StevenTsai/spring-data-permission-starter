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

    @Test
    void builder_immutableOrgIdList_isAccepted() {
        // Regression: List.of(...) throws NPE from contains(null) on immutable lists.
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(List.of(10L, 20L))
                .build();

        assertThat(context.getAccessibleOrgIds()).containsExactly(10L, 20L);
    }

    @Test
    void builder_duplicateOrgIds_areDeduplicatedPreservingOrder() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(Arrays.asList(20L, 10L, 20L, 30L, 10L))
                .build();

        assertThat(context.getAccessibleOrgIds()).containsExactly(20L, 10L, 30L);
    }

    @Test
    void builder_nullOrgIdElement_throwsException() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(Arrays.asList(10L, null))
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("null elements")
                .hasMessageContaining("OWN_ORG");
    }

    @Test
    void builder_emptyOrgIds_isDenyAll() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(Collections.emptyList())
                .build();

        assertThat(context.getAccessibleOrgIds()).isEmpty();
        assertThat(context.isDenyAll()).isTrue();
    }

    @Test
    void builder_nonEmptyOrgIds_isNotDenyAll() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(List.of(10L))
                .build();

        assertThat(context.isDenyAll()).isFalse();
    }

    @Test
    void builder_allScope_ignoresOrgIdsAndOwner() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.ALL)
                .accessibleOrgIds(List.of(10L))
                .ownerUserId(99L)
                .build();

        assertThat(context.getAccessibleOrgIds()).isNull();
        assertThat(context.getOwnerUserId()).isNull();
        assertThat(context.isDenyAll()).isFalse();
    }

    @Test
    void builder_selfScope_nullOrgIds_becomesEmptyList() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(StandardDataScope.SELF)
                .ownerUserId(1L)
                .build();

        assertThat(context.getAccessibleOrgIds()).isEmpty();
        assertThat(context.isDenyAll()).isFalse();
    }

    @Test
    void builder_blankScopeCode_throwsException() {
        DataScope blankScope = new DataScope() {
            @Override
            public String getCode() {
                return " ";
            }
        };

        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(blankScope)
                .accessibleOrgIds(Collections.emptyList())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("getCode()");
    }

    @Test
    void builder_customOrgScope_requiresOrgIds() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(new CustomOrgScope())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("accessibleOrgIds")
                .hasMessageContaining("CUSTOM_ORG");
    }

    @Test
    void builder_customAllScope_needsNoOrgIds() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(new CustomAllScope())
                .build();

        assertThat(context.isAllScope()).isTrue();
        assertThat(context.isSelfScope()).isFalse();
    }

    @Test
    void builder_customSelfScope_requiresOwnerUserId() {
        assertThatThrownBy(() -> DataPermissionContext.builder()
                .operatorUserId(1L)
                .scope(new CustomSelfScope())
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ownerUserId")
                .hasMessageContaining("CUSTOM_SELF");
    }

    @Test
    void toString_containsResolvedValues() {
        DataPermissionContext context = DataPermissionContext.builder()
                .operatorUserId(7L)
                .scope(StandardDataScope.OWN_ORG)
                .accessibleOrgIds(List.of(10L))
                .build();

        assertThat(context.toString())
                .contains("operatorUserId=7")
                .contains("scope=OWN_ORG")
                .contains("accessibleOrgIds=[10]");
    }

    private static final class CustomOrgScope implements DataScope {
        @Override
        public String getCode() {
            return "CUSTOM_ORG";
        }
    }

    private static final class CustomAllScope implements DataScope {
        @Override
        public String getCode() {
            return "CUSTOM_ALL";
        }

        @Override
        public boolean isAll() {
            return true;
        }
    }

    private static final class CustomSelfScope implements DataScope {
        @Override
        public String getCode() {
            return "CUSTOM_SELF";
        }

        @Override
        public boolean isSelf() {
            return true;
        }
    }
}
