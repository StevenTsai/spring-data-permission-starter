package io.github.steventsai.permission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional entity-level override for the permission column names.
 * <p>
 * When an entity class is annotated, {@link DataPermissionHelper#createQuery(Class, PermissionRequest)}
 * uses the configured columns for that entity instead of the global
 * {@code spring.data.permission.*} defaults. Empty values fall back to the
 * global configuration.
 *
 * <pre>{@code
 * @DataPermissionTable(orgIdColumn = "dept_id", ownerUserIdColumn = "creator_id")
 * public class Order { ... }
 * }</pre>
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermissionTable {

    /**
     * Organization ID column name. Empty means "use the global default".
     *
     * @return the column name
     */
    String orgIdColumn() default "";

    /**
     * Owner user ID column name. Empty means "use the global default".
     *
     * @return the column name
     */
    String ownerUserIdColumn() default "";
}
