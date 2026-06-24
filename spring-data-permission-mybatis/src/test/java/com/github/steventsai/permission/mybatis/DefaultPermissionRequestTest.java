package com.github.steventsai.permission.mybatis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPermissionRequestTest {

    @Test
    void operatorUserId_returnsConstructorValue() {
        DefaultPermissionRequest request = new DefaultPermissionRequest(42L);

        assertThat(request.operatorUserId()).isEqualTo(42L);
    }

    @Test
    void toString_containsUserId() {
        DefaultPermissionRequest request = new DefaultPermissionRequest(42L);

        assertThat(request).hasToString("DefaultPermissionRequest{operatorUserId=42}");
    }
}
