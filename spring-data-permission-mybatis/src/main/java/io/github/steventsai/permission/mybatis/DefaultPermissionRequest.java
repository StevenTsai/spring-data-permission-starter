package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionRequest;

/**
 * Default implementation of {@link PermissionRequest}.
 * <p>
 * Created by {@link PermissionArgumentResolver} from the HTTP request context.
 */
public class DefaultPermissionRequest implements PermissionRequest {

    private final Long operatorUserId;

    public DefaultPermissionRequest(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    @Override
    public Long operatorUserId() {
        return operatorUserId;
    }

    @Override
    public String toString() {
        return "DefaultPermissionRequest{operatorUserId=" + operatorUserId + "}";
    }
}
