package com.github.steventsai.permission.mybatis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for controller method parameters to inject a {@link com.github.steventsai.permission.PermissionRequest}.
 * <p>
 * The framework resolves the current user's identity from the HTTP request
 * (e.g. from session token, JWT, or custom header) and creates a
 * {@link PermissionRequest} instance automatically.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @GetMapping("/orders")
 * public List<Order> listOrders(@CurrentPermission PermissionRequest request) {
 *     return orderService.listOrders(request);
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentPermission {
}
