# Release Notes

## v0.4.0

`spring-data-permission-starter` 0.4.0 focuses on **fail-closed behavior** and **integration safety**.

This release closes the risk of an empty organization scope degrading into an unfiltered query, makes the header-based identity resolver opt-in, and adds entity-level column overrides, custom request types, and large-org-tree safeguards.

## Highlights

- An empty organization scope is deny-all by default and emits `and 1=0`
- The header identity resolver is opt-in, preventing `X-User-Id` spoofing in production
- New `@DataPermissionTable` for entity-level org/owner column overrides
- New `PermissionRequestFactory` for custom `PermissionRequest` subtypes
- New `DataPermissionTarget` to avoid argument-order mistakes in single-record checks
- New `max-org-ids` limit to bound oversized `IN (...)` predicates
- Permission parameters are reserved and cannot be overridden by business parameters

## Included in This Release

### Core

- `DataPermissionTarget` record and `checkAccess(request, target)` overload
- Custom `DataScope` implementations can opt into built-in ALL/SELF semantics via `isAll()` / `isSelf()`
- `DataPermissionContext` now handles immutable lists, duplicate IDs, empty lists, and null elements explicitly
- `AccessDeniedException` is annotated with `@ResponseStatus(FORBIDDEN)`

### MyBatis

- Entity-level column overrides through `@DataPermissionTable`
- `max-org-ids` validation in `PermissionSqlParamAssembler`
- Reserved-key protection and immutable snapshots in `PermissionQueryBuilder`
- `ScopeCondition` uses explicit `_allScope` / `_selfScope` / `_denyAll` flags

### Spring MVC / Spring Boot

- New `PermissionWebAutoConfiguration`
- `header-resolver-enabled` defaults to `false`
- New `PermissionRequestFactory` extension point
- Auto-configuration split into core and optional web configuration

### Sample and testing

- Sample now covers deny-all and unknown users
- Unauthorized single-record access returns 404 to avoid enumeration
- New tests for the request factory, web auto-configuration, entity overrides, deny-all, and reserved keys
- CI now builds the sample modules

## Breaking Changes

- The header resolver is no longer registered by default; set `spring.data.permission.header-resolver-enabled=true`
- `StandardDataScope.fromCode()` now throws `IllegalArgumentException` for blank/unknown codes
- An empty `accessibleOrgIds` list now means deny-all instead of "no filter"
- `authorizedOrgIds`, `ownerUserId`, and `_*` parameters are reserved and cannot be overridden

## Upgrading from 0.3.x

1. If you still use the demo header resolver, enable `spring.data.permission.header-resolver-enabled=true`
2. Verify stored `DataScope` values; unknown values now fail fast
3. Review `PermissionResolver`: an empty organization scope now denies access instead of returning everything
4. Review business parameter maps; do not use framework-reserved keys
5. Prefer `DataPermissionTarget.of(orgId, ownerId)` for single-record checks

## Verification

- `mvn clean verify -B -Pwith-samples` passes
- JaCoCo line coverage is at least 80% for the core and mybatis modules
- Checkstyle reports zero violations

## Current Boundaries

- Still targets explicit `Spring Boot + MyBatis XML` SQL
- Does not rewrite non-XML query paths automatically
- Mapper XML must still receive `@Param("params") Map<String, Object>`

## Feedback Welcome

Feedback on large organization trees, multi-tenant extensions, custom scopes, and real production integrations is welcome.
