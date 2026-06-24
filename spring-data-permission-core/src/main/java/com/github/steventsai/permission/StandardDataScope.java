package com.github.steventsai.permission;

/**
 * Standard data scope levels covering 95% of B2B scenarios.
 * <ul>
 *   <li>{@code ALL} — No restriction, see all data</li>
 *   <li>{@code OWN_ORG} — See data belonging to the user's own organization</li>
 *   <li>{@code OWN_AND_CHILDREN} — See data belonging to the user's organization and all child organizations</li>
 *   <li>{@code SELF} — See only data personally owned by the user</li>
 * </ul>
 */
public enum StandardDataScope implements DataScope {

    ALL("ALL"),
    OWN_ORG("OWN_ORG"),
    OWN_AND_CHILDREN("OWN_AND_CHILDREN"),
    SELF("SELF");

    private final String code;

    StandardDataScope(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    /**
     * Resolve a StandardDataScope from its code string.
     *
     * @param code the scope code (e.g. "ALL", "OWN_ORG")
     * @return the matching scope, or null if not found
     */
    public static StandardDataScope fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (StandardDataScope scope : values()) {
            if (scope.code.equals(code)) {
                return scope;
            }
        }
        return null;
    }
}
