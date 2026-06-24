package io.github.steventsai.permission;

/**
 * Data scope interface. Implement this to define custom data access levels.
 * <p>
 * The library provides {@link StandardDataScope} with 4 standard levels.
 * Implement this interface only if you need custom scopes beyond the standard ones.
 */
public interface DataScope {

    /**
     * Return the unique code of this data scope.
     * Used for serialization and database storage.
     */
    String getCode();
}
