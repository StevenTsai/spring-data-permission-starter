package io.github.steventsai.permission;

/**
 * Standard data scope levels covering the most common B2B scenarios.
 * <ul>
 *   <li>{@code ALL} — No restriction, see all data</li>
 *   <li>{@code OWN_ORG} — See data belonging to the user's own organization</li>
 *   <li>{@code OWN_AND_CHILDREN} — See data belonging to the user's organization and all child organizations</li>
 *   <li>{@code SELF} — See only data personally owned by the user</li>
 * </ul>
 */
public enum StandardDataScope implements DataScope {

    ALL("ALL", true, false),
    OWN_ORG("OWN_ORG", false, false),
    OWN_AND_CHILDREN("OWN_AND_CHILDREN", false, false),
    SELF("SELF", false, true);

    private final String code;
    private final boolean all;
    private final boolean self;

    StandardDataScope(String code, boolean all, boolean self) {
        this.code = code;
        this.all = all;
        this.self = self;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public boolean isAll() {
        return all;
    }

    @Override
    public boolean isSelf() {
        return self;
    }

    /**
     * Resolve a {@code StandardDataScope} from its code string.
     * <p>
     * Matching is case-insensitive and ignores surrounding whitespace.
     *
     * @param code the scope code (e.g. {@code "ALL"}, {@code "own_org"})
     * @return the matching scope, or {@code null} when {@code code} is {@code null}
     * @throws IllegalArgumentException when the code is blank or unknown
     */
    public static StandardDataScope fromCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Data scope code must not be blank");
        }
        for (StandardDataScope scope : values()) {
            if (scope.code.equalsIgnoreCase(normalized)) {
                return scope;
            }
        }
        throw new IllegalArgumentException(
                "Unknown data scope code: '" + code + "'. Supported values are: "
                        + "ALL, OWN_ORG, OWN_AND_CHILDREN, SELF");
    }
}
