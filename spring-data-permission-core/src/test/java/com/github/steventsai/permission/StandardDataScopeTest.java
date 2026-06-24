package com.github.steventsai.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    void fromCode_unknownCode_returnsNull() {
        assertThat(StandardDataScope.fromCode("UNKNOWN")).isNull();
    }

    @Test
    void fromCode_emptyString_returnsNull() {
        assertThat(StandardDataScope.fromCode("")).isNull();
    }

    @Test
    void values_containsFourScopes() {
        assertThat(StandardDataScope.values()).hasSize(4);
    }
}
