package io.github.steventsai.permission;

/**
 * A data access scope.
 * <p>
 * The library provides {@link StandardDataScope} with four standard levels. Custom
 * scopes are supported as well: by default a custom scope is treated as an
 * organization-scoped permission and therefore requires a non-null
 * {@code accessibleOrgIds} collection.
 * <p>
 * A custom scope may override {@link #isAll()} or {@link #isSelf()} when its
 * semantics match one of those built-in modes. This keeps custom scopes
 * compatible with the SQL fragment without requiring MyBatis-specific code in
 * the core module.
 */
public interface DataScope {

    /**
     * Return the unique code of this data scope.
     * Used for serialization, logging, and database storage.
     *
     * @return the non-null scope code
     */
    String getCode();

    /**
     * Whether this scope grants unrestricted access.
     *
     * @return {@code true} when no row-level filter should be applied
     */
    default boolean isAll() {
        return false;
    }

    /**
     * Whether this scope restricts access to rows owned by the operator.
     *
     * @return {@code true} when filtering should use the owner user ID
     */
    default boolean isSelf() {
        return false;
    }
}
