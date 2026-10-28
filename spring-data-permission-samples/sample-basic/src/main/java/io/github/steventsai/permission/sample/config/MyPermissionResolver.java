package io.github.steventsai.permission.sample.config;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.DataScope;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.PermissionResolver;
import io.github.steventsai.permission.StandardDataScope;
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
     * - User 5 (contractor): OWN_ORG scope with no organizations (deny-all)
     */
    private static final Map<Long, UserPermission> USER_PERMISSIONS = Map.of(
            1L, new UserPermission(StandardDataScope.ALL, List.of()),
            2L, new UserPermission(StandardDataScope.OWN_ORG, List.of(1L)),
            3L, new UserPermission(StandardDataScope.OWN_AND_CHILDREN, List.of(1L, 2L)),
            4L, new UserPermission(StandardDataScope.SELF, Collections.emptyList()),
            5L, new UserPermission(StandardDataScope.OWN_ORG, Collections.emptyList())
    );

    @Override
    public DataPermissionContext resolve(PermissionRequest request) {
        Long userId = request.operatorUserId();

        UserPermission perm = USER_PERMISSIONS.get(userId);
        if (perm == null) {
            // Unknown users are denied by default, not granted SELF access to records.
            return DataPermissionContext.builder()
                    .operatorUserId(userId)
                    .scope(StandardDataScope.OWN_ORG)
                    .accessibleOrgIds(Collections.emptyList())
                    .build();
        }

        DataPermissionContext.Builder builder = DataPermissionContext.builder()
                .operatorUserId(userId)
                .scope(perm.scope)
                .accessibleOrgIds(perm.accessibleOrgIds);

        if (perm.scope.isSelf()) {
            builder.ownerUserId(userId);
        }

        return builder.build();
    }

    private record UserPermission(DataScope scope, List<Long> accessibleOrgIds) {
    }
}
