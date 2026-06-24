package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.exception.MissingPermissionException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link PermissionRequest} parameters annotated with {@link CurrentPermission}.
 * <p>
 * By default, extracts the user ID from the HTTP header "X-User-Id".
 * Override this class to implement custom authentication logic (e.g. JWT parsing).
 * <p>
 * Throws {@link MissingPermissionException} (HTTP 400) when the header is missing or invalid.
 */
public class PermissionArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String DEFAULT_USER_ID_HEADER = "X-User-Id";

    private final String userIdHeader;

    public PermissionArgumentResolver() {
        this(DEFAULT_USER_ID_HEADER);
    }

    /**
     * @param userIdHeader the HTTP header name containing the user ID
     */
    public PermissionArgumentResolver(String userIdHeader) {
        this.userIdHeader = userIdHeader;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentPermission.class)
                && PermissionRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) {
        String userIdStr = webRequest.getHeader(userIdHeader);
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new MissingPermissionException(
                    "Missing user ID in request header '" + userIdHeader + "'. "
                            + "Ensure your authentication layer sets this header.");
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdStr.trim());
        } catch (NumberFormatException e) {
            throw new MissingPermissionException(
                    "Invalid user ID in request header '" + userIdHeader + "': " + userIdStr, e);
        }

        return new DefaultPermissionRequest(userId);
    }
}
