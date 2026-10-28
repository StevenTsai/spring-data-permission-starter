package io.github.steventsai.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataPermissionTargetTest {

    @Test
    void accessors_returnConstructorValues() {
        DataPermissionTarget target = new DataPermissionTarget(10L, 20L);

        assertThat(target.orgId()).isEqualTo(10L);
        assertThat(target.ownerId()).isEqualTo(20L);
    }

    @Test
    void of_createsTargetWithSameValues() {
        DataPermissionTarget target = DataPermissionTarget.of(10L, 20L);

        assertThat(target).isEqualTo(new DataPermissionTarget(10L, 20L));
    }

    @Test
    void of_allowsNullValues() {
        DataPermissionTarget target = DataPermissionTarget.of(null, null);

        assertThat(target.orgId()).isNull();
        assertThat(target.ownerId()).isNull();
    }
}
