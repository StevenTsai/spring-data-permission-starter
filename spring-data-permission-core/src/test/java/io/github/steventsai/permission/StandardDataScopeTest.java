package io.github.steventsai.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandardDataScopeTest {

    @Test
    void allScope_hasCorrectCode() {
        assertThat(StandardDataScope.ALL.getCode()).isEqualTo("ALL");
    }

    @Test
    void ownOrgScope_hasCorrectCode() {
        assertThat(StandardDataScope.OWN_ORG.getCode()).isEqualTo("OWN_ORG");
    }

    @Test
    void ownAndChildrenScope_hasCorrectCode() {
        assertThat(StandardDataScope.OWN_AND_CHILDREN.getCode()).isEqualTo("OWN_AND_CHILDREN");
    }

    @Test
    void selfScope_hasCorrectCode() {
        assertThat(StandardDataScope.SELF.getCode()).isEqualTo("SELF");
    }

    @Test
    void fromCode_validCode_returnsScope() {
        assertThat(StandardDataScope.fromCode("ALL")).isEqualTo(StandardDataScope.ALL);
        assertThat(StandardDataScope.fromCode("OWN_ORG")).isEqualTo(StandardDataScope.OWN_ORG);
        assertThat(StandardDataScope.fromCode("OWN_AND_CHILDREN")).isEqualTo(StandardDataScope.OWN_AND_CHILDREN);
        assertThat(StandardDataScope.fromCode("SELF")).isEqualTo(StandardDataScope.SELF);
    }

    @Test
    void fromCode_null_returnsNull() {
        assertThat(StandardDataScope.fromCode(null)).isNull();
    }

    @Test
    void fromCode_unknownCode_throwsException() {
        assertThatThrownBy(() -> StandardDataScope.fromCode("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown data scope code")
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void fromCode_blankCode_throwsException() {
        assertThatThrownBy(() -> StandardDataScope.fromCode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
        assertThatThrownBy(() -> StandardDataScope.fromCode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    void fromCode_isCaseInsensitiveAndTrimsWhitespace() {
        assertThat(StandardDataScope.fromCode("all")).isEqualTo(StandardDataScope.ALL);
        assertThat(StandardDataScope.fromCode(" own_org ")).isEqualTo(StandardDataScope.OWN_ORG);
        assertThat(StandardDataScope.fromCode("Own_And_Children"))
                .isEqualTo(StandardDataScope.OWN_AND_CHILDREN);
        assertThat(StandardDataScope.fromCode("self")).isEqualTo(StandardDataScope.SELF);
    }

    @Test
    void isAll_trueOnlyForAllScope() {
        assertThat(StandardDataScope.ALL.isAll()).isTrue();
        assertThat(StandardDataScope.OWN_ORG.isAll()).isFalse();
        assertThat(StandardDataScope.OWN_AND_CHILDREN.isAll()).isFalse();
        assertThat(StandardDataScope.SELF.isAll()).isFalse();
    }

    @Test
    void isSelf_trueOnlyForSelfScope() {
        assertThat(StandardDataScope.SELF.isSelf()).isTrue();
        assertThat(StandardDataScope.ALL.isSelf()).isFalse();
        assertThat(StandardDataScope.OWN_ORG.isSelf()).isFalse();
        assertThat(StandardDataScope.OWN_AND_CHILDREN.isSelf()).isFalse();
    }

    @Test
    void values_containsFourScopes() {
        assertThat(StandardDataScope.values()).hasSize(4);
    }
}
