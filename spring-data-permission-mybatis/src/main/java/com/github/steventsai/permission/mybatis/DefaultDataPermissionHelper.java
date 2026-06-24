package com.github.steventsai.permission.mybatis;

import com.github.steventsai.permission.DataPermissionContext;
import com.github.steventsai.permission.DataPermissionHelper;
import com.github.steventsai.permission.PermissionQueryBuilder;
import com.github.steventsai.permission.PermissionRequest;
import com.github.steventsai.permission.PermissionResolver;
import com.github.steventsai.permission.exception.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link DataPermissionHelper}.
 * <p>
 * Delegates permission resolution to the user-provided {@link PermissionResolver}
 * and uses {@link PermissionSqlParamAssembler} to convert context into SQL parameters.
 */
public class DefaultDataPermissionHelper implements DataPermissionHelper {

    private static final Logger log = LoggerFactory.getLogger(DefaultDataPermissionHelper.class);

    private final PermissionResolver permissionResolver;
    private final PermissionSqlParamAssembler paramAssembler;

    public DefaultDataPermissionHelper(PermissionResolver permissionResolver,
                                        PermissionSqlParamAssembler paramAssembler) {
        this.permissionResolver = permissionResolver;
        this.paramAssembler = paramAssembler;
    }

    @Override
    public <T> PermissionQueryBuilder<T> createQuery(Class<T> entityClass, PermissionRequest request) {
        DataPermissionContext context = resolveContext(request);
        Map<String, Object> sqlParams = paramAssembler.assemble(context);
        return new DefaultPermissionQueryBuilder<>(sqlParams);
    }

    @Override
    public void checkAccess(PermissionRequest request, Long orgId, Long ownerId) {
        DataPermissionContext context = resolveContext(request);

        // ALL scope: always pass
        if (context.isAllScope()) {
            return;
        }

        // SELF scope: check owner user ID
        if (context.isSelfScope()) {
            Long currentUserId = context.getOperatorUserId();
            boolean matched = false;
            if (ownerId != null && currentUserId.equals(ownerId)) {
                matched = true;
            }
            if (!matched) {
                log.debug("Access denied: SELF scope, userId={}, ownerId={}", currentUserId, ownerId);
                throw new AccessDeniedException(context.getScope().getCode(), orgId, ownerId);
            }
            return;
        }

        // OWN_ORG / OWN_AND_CHILDREN: check org ID
        List<Long> authorizedOrgIds = context.getAccessibleOrgIds();
        if (authorizedOrgIds == null || authorizedOrgIds.isEmpty()) {
            // Should not happen for non-ALL scope, but treat as no access
            log.warn("Access denied: empty authorizedOrgIds for scope={}", context.getScope());
            throw new AccessDeniedException(context.getScope().getCode(), orgId, ownerId);
        }
        if (orgId == null || !authorizedOrgIds.contains(orgId)) {
            log.debug("Access denied: orgId={} not in authorizedOrgIds={}", orgId, authorizedOrgIds);
            throw new AccessDeniedException(context.getScope().getCode(), orgId, ownerId);
        }
    }

    @Override
    public DataPermissionContext resolveContext(PermissionRequest request) {
        return permissionResolver.resolve(request);
    }
}
