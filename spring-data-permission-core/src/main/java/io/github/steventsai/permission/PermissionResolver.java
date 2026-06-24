package io.github.steventsai.permission;

/**
 * Resolves a {@link DataPermissionContext} from a {@link PermissionRequest}.
 * <p>
 * This is the main extension point. Each application must provide exactly one
 * implementation that knows how to look up the current user's data scope and
 * resolve accessible organization IDs.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @Component
 * public class MyPermissionResolver implements PermissionResolver {
 *
 *     @Autowired
 *     private UserMapper userMapper;
 *
 *     @Override
 *     public DataPermissionContext resolve(PermissionRequest request) {
 *         User user = userMapper.selectById(request.operatorUserId());
 *         List<Long> orgIds = resolveOrgIds(user.getOrgId(), user.getDataScope());
 *         return DataPermissionContext.builder()
 *             .operatorUserId(request.operatorUserId())
 *             .scope(StandardDataScope.fromCode(user.getDataScope()))
 *             .accessibleOrgIds(orgIds)
 *             .ownerUserId(request.operatorUserId())
 *             .build();
 *     }
 * }
 * }</pre>
 */
public interface PermissionResolver {

    /**
     * Resolve the data permission context for the given request.
     *
     * @param request the permission request containing at least the operator user ID
     * @return an immutable permission context
     */
    DataPermissionContext resolve(PermissionRequest request);
}
