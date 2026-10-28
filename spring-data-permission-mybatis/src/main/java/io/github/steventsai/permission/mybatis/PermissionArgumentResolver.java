package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.exception.MissingPermissionException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

/**
 * Resolves {@link PermissionRequest} parameters annotated with {@link CurrentPermission}.
 * <p>
 * This resolver is a <strong>demo/opt-in</strong> adapter: it extracts the user
 * ID from one HTTP header and must be enabled explicitly with
 * {@code spring.data.permission.header-resolver-enabled=true}. Never enable it
 * in production without an upstream gateway that strips and rewrites the header,
 * because clients can otherwise spoof their identity. Prefer a custom resolver
 * backed by your JWT/session authentication for production.
 * <p>
 * Throws {@link MissingPermissionException} (HTTP 400) when the header is missing or invalid.
 */
public class PermissionArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String DEFAULT_USER_ID_HEADER = "X-User-Id";

    private final String userIdHeader;
    private final PermissionRequestFactory requestFactory;

    public PermissionArgumentResolver() {
        this(DEFAULT_USER_ID_HEADER);
    }

    /**
     * @param userIdHeader the HTTP header name containing the user ID
     */
    public PermissionArgumentResolver(String userIdHeader) {
        this(userIdHeader, PermissionRequestFactory.defaultFactory());
    }

    /**
     * @param userIdHeader   the HTTP header name containing the user ID
     * @param requestFactory factory used to create the request object
     */
    public PermissionArgumentResolver(String userIdHeader, PermissionRequestFactory requestFactory) {
        this.userIdHeader = Objects.requireNonNull(userIdHeader, "userIdHeader must not be null");
        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory must not be null");
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentPermission.class)
                && PermissionRequest.class.isAssignableFrom(parameter.getParameterType())
                && requestFactory.supports(parameter.getParameterType());
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
        } catch (NumberFormatException ex) {
            // Do not echo the raw header value; it may contain sensitive or attacker-controlled data.
            throw new MissingPermissionException(
                    "Invalid user ID in request header '" + userIdHeader
                            + "'. Expected a numeric user ID.", ex);
        }

        return requestFactory.create(userId, parameter);
    }
}
