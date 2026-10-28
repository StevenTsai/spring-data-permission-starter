package io.github.steventsai.permission.mybatis;

import io.github.steventsai.permission.PermissionRequest;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Constructor;

/**
 * Creates a {@link PermissionRequest} for a controller method parameter.
 * <p>
 * The default implementation supports {@link PermissionRequest}/{@link DefaultPermissionRequest}
 * and concrete request types with a public {@code (Long)} constructor. Applications
 * that need tenant context can provide their own factory bean.
 */
@FunctionalInterface
public interface PermissionRequestFactory {

    /**
     * Create a request for the current user.
     *
     * @param userId    the authenticated user ID
     * @param parameter the controller method parameter
     * @return a request instance
     */
    PermissionRequest create(Long userId, MethodParameter parameter);

    /**
     * Whether this factory can create the given parameter type.
     *
     * @param parameterType the declared controller parameter type
     * @return {@code true} when the type is supported
     */
    default boolean supports(Class<?> parameterType) {
        return parameterType != null
                && PermissionRequest.class.isAssignableFrom(parameterType)
                && (parameterType.isAssignableFrom(DefaultPermissionRequest.class)
                        || hasLongConstructor(parameterType));
    }

    /**
     * Create the default factory.
     *
     * @return a factory that uses {@link DefaultPermissionRequest} or a {@code (Long)} constructor
     */
    static PermissionRequestFactory defaultFactory() {
        return (userId, parameter) -> {
            Class<?> type = parameter == null
                    ? DefaultPermissionRequest.class
                    : parameter.getParameterType();

            if (PermissionRequest.class.isAssignableFrom(type)
                    && type.isAssignableFrom(DefaultPermissionRequest.class)) {
                return new DefaultPermissionRequest(userId);
            }

            Constructor<?> constructor = findLongConstructor(type);
            if (constructor == null) {
                throw new IllegalArgumentException(
                        "Unsupported @CurrentPermission parameter type: " + type.getName()
                                + ". Use PermissionRequest/DefaultPermissionRequest, add a public (Long) "
                                + "constructor, or provide a custom PermissionRequestFactory bean.");
            }
            try {
                return (PermissionRequest) constructor.newInstance(userId);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(
                        "Failed to instantiate permission request type: " + type.getName(), ex);
            }
        };
    }

    private static boolean hasLongConstructor(Class<?> type) {
        return findLongConstructor(type) != null;
    }

    private static Constructor<?> findLongConstructor(Class<?> type) {
        try {
            return type.getConstructor(Long.class);
        } catch (NoSuchMethodException ignored) {
            try {
                return type.getConstructor(long.class);
            } catch (NoSuchMethodException alsoIgnored) {
                return null;
            }
        }
    }
}
