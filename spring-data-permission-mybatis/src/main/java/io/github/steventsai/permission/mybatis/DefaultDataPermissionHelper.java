package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.DataPermissionContext;
import io.github.steventsai.permission.DataPermissionHelper;
import io.github.steventsai.permission.DataPermissionTarget;
import io.github.steventsai.permission.PermissionQueryBuilder;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.PermissionResolver;
import io.github.steventsai.permission.exception.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        this.permissionResolver = Objects.requireNonNull(permissionResolver, "permissionResolver");
        this.paramAssembler = Objects.requireNonNull(paramAssembler, "paramAssembler");
    }

    @Override
    public <T> PermissionQueryBuilder<T> createQuery(Class<T> entityClass, PermissionRequest request) {
        Objects.requireNonNull(entityClass, "entityClass must not be null");
        DataPermissionContext context = resolveContext(request);
        Map<String, Object> sqlParams = paramAssembler.assemble(context, entityClass);
        return new DefaultPermissionQueryBuilder<>(sqlParams);
    }

    @Override
    public void checkAccess(PermissionRequest request, DataPermissionTarget target) {
        Objects.requireNonNull(target, "target must not be null");
        checkAccess(request, target.orgId(), target.ownerId());
    }

    @Override
    public void checkAccess(PermissionRequest request, Long orgId, Long ownerId) {
        DataPermissionContext context = resolveContext(request);

        // ALL scope: always pass
        if (context.isAllScope()) {
            return;
        }

        // SELF scope: compare the row owner with the context owner (not the operator).
        if (context.isSelfScope()) {
            Long currentOwnerId = context.getOwnerUserId();
            if (ownerId == null || !ownerId.equals(currentOwnerId)) {
                log.debug("Access denied: SELF scope, contextOwnerId={}, rowOwnerId={}",
                        currentOwnerId, ownerId);
                throw new AccessDeniedException(context.getScope().getCode(), orgId, ownerId);
            }
            return;
        }

        // Organization-scoped access: an empty list is deny-all, never "allow all".
        List<Long> authorizedOrgIds = context.getAccessibleOrgIds();
        if (authorizedOrgIds == null || authorizedOrgIds.isEmpty()) {
            log.debug("Access denied: empty authorizedOrgIds for scope={}", context.getScope().getCode());
            throw new AccessDeniedException(context.getScope().getCode(), orgId, ownerId);
        }
        if (orgId == null || !authorizedOrgIds.contains(orgId)) {
            log.debug("Access denied: orgId={} not in authorizedOrgIds", orgId);
            throw new AccessDeniedException(context.getScope().getCode(), orgId, ownerId);
        }
    }

    @Override
    public DataPermissionContext resolveContext(PermissionRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        DataPermissionContext context = permissionResolver.resolve(request);
        if (context == null) {
            throw new IllegalStateException(
                    "PermissionResolver returned null; a DataPermissionContext is required");
        }
        return context;
    }
}
