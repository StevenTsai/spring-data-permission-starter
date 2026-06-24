package com.github.steventsai.permission.sample.config;

import com.github.steventsai.permission.DataPermissionContext;
import com.github.steventsai.permission.PermissionRequest;
import com.github.steventsai.permission.PermissionResolver;
import com.github.steventsai.permission.StandardDataScope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sample PermissionResolver implementation.
 * <p>
 * In a real application, this would query the database to look up
 * the user's data scope and resolve accessible organization IDs.
 * <p>
 * This sample uses a hardcoded map for demonstration purposes.
 */
@Component
public class MyPermissionResolver implements PermissionResolver {

    /**
     * Simulated user-org mapping:
     * - User 1 (admin): ALL scope
     * - User 2 (manager): OWN_ORG scope, belongs to org 1
     * - User 3 (manager): OWN_AND_CHILDREN scope, belongs to org 1 (has child org 2)
     * - User 4 (staff): SELF scope, belongs to org 2
     */
    private static final Map<Long, UserPermission> USER_PERMISSIONS = Map.of(
            1L, new UserPermission(StandardDataScope.ALL, 1L, List.of(1L, 2L, 3L)),
            2L, new UserPermission(StandardDataScope.OWN_ORG, 1L, List.of(1L)),
            3L, new UserPermission(StandardDataScope.OWN_AND_CHILDREN, 1L, List.of(1L, 2L)),
            4L, new UserPermission(StandardDataScope.SELF, 2L, Collections.emptyList())
    );

    @Override
    public DataPermissionContext resolve(PermissionRequest request) {
        Long userId = request.operatorUserId();

        UserPermission perm = USER_PERMISSIONS.get(userId);
        if (perm == null) {
            // Default: no access
            return DataPermissionContext.builder()
                    .operatorUserId(userId)
                    .scope(StandardDataScope.SELF)
                    .accessibleOrgIds(Collections.emptyList())
                    .ownerUserId(userId)
                    .build();
        }

        DataPermissionContext.Builder builder = DataPermissionContext.builder()
                .operatorUserId(userId)
                .scope(perm.scope)
                .accessibleOrgIds(perm.accessibleOrgIds);

        if (perm.scope == StandardDataScope.SELF) {
            builder.ownerUserId(userId);
        }

        return builder.build();
    }

    private record UserPermission(StandardDataScope scope, Long orgId, List<Long> accessibleOrgIds) {}
}
