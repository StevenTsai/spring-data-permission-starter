package io.github.steventsai.permission.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccessDeniedExceptionTest {

    @Test
    void messageConstructor_setsOnlyMessage() {
        AccessDeniedException exception = new AccessDeniedException("denied");

        assertThat(exception).hasMessage("denied");
        assertThat(exception.getScope()).isNull();
        assertThat(exception.getOrgId()).isNull();
        assertThat(exception.getOwnerId()).isNull();
    }

    @Test
    void scopeConstructor_formatsMessageAndFields() {
        AccessDeniedException exception = new AccessDeniedException("SELF", 10L, 20L);

        assertThat(exception).hasMessage("Access denied: scope=SELF, orgId=10, ownerId=20");
        assertThat(exception.getScope()).isEqualTo("SELF");
        assertThat(exception.getOrgId()).isEqualTo(10L);
        assertThat(exception.getOwnerId()).isEqualTo(20L);
    }
}
