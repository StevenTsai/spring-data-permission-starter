# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [0.4.0] - 2026-10-28

### Added

- `DataPermissionTarget` record and `checkAccess(request, target)` overload to avoid swapping the two `Long` arguments
- `@DataPermissionTable` for entity-level org/owner column overrides
- `PermissionRequestFactory` for injecting custom `PermissionRequest` subtypes into controller parameters
- Opt-in `PermissionWebAutoConfiguration`; the header-based `PermissionArgumentResolver` is now registered only when `spring.data.permission.header-resolver-enabled=true`
- `spring.data.permission.max-org-ids` (default `1000`, `0` = unlimited) to bound `IN (...)` predicates
- Fail-closed deny-all semantics: an organization-scoped context with an empty org list now emits `and 1=0`
- Custom `DataScope.isAll()` / `isSelf()` hooks so custom scopes work with the built-in SQL fragment
- Reserved SQL parameter keys (`authorizedOrgIds`, `ownerUserId`, `_*`) that business parameters cannot override
- `@ResponseStatus(FORBIDDEN)` on `AccessDeniedException` for Spring MVC default handling
- Integration coverage for deny-all users, unknown users, and anti-enumeration 404 responses

### Changed

- `StandardDataScope.fromCode()` is now case-insensitive, trims whitespace, and throws `IllegalArgumentException` for blank or unknown codes instead of returning `null`
- Column-name validation accepts multi-part identifiers such as `app.t.org_id` and rejects malformed forms
- `PermissionQueryBuilder.getSqlParams()` returns an immutable snapshot
- `PermissionSqlParamAssembler` accepts an entity class to apply `@DataPermissionTable`
- SELF-scope access checks compare the row owner against the resolved context owner instead of the operator
- Upgraded to Spring Boot 3.5.16 and MyBatis Spring Boot Starter 3.0.5
- CI now builds the sample modules (`-Pwith-samples`)

### Fixed

- `DataPermissionContext` threw `NullPointerException` when `accessibleOrgIds` was an immutable `List.of(...)`
- Empty organization lists could produce an unfiltered query instead of denying access
- `PermissionRequestFactory.supports()` treated `Object` and other supertypes as supported parameter types
- A `PermissionResolver` returning `null` now fails fast with a clear error

### Breaking Changes

- The header-based argument resolver is disabled by default; enable it explicitly or provide your own resolver
- `StandardDataScope.fromCode()` no longer returns `null` for unknown/blank codes
- An empty `accessibleOrgIds` list is now deny-all rather than "no filter"
- Permission parameter keys are reserved and cannot be overwritten through `PermissionQueryBuilder`

## [0.3.0] - 2026-06-25

### Fixed

- Upgrade `central-publishing-maven-plugin` to 0.11.0 to fix Maven Central validation errors
- Exclude sample modules from default deployment to Maven Central

## [0.2.0] - 2026-06-25

### Fixed

- Upgrade `central-publishing-maven-plugin` from 0.4.0 to fix multi-module artifact path issues

## [0.1.0] - 2026-06-25

### Added

- Core abstractions: `DataScope`, `StandardDataScope`, `PermissionRequest`, `PermissionResolver`, `DataPermissionContext`, `DataPermissionHelper`, `PermissionQueryBuilder`
- `AccessDeniedException` for single-record access violations
- MyBatis integration: `PermissionSqlParamAssembler`, `DefaultDataPermissionHelper`, `DefaultPermissionQueryBuilder`
- `PermissionArgumentResolver` for extracting user ID from HTTP headers
- `@CurrentPermission` annotation for controller parameters
- `PermissionProperties` for externalized configuration
- MyBatis SQL fragment (`data-permission-fragment.xml`) for automatic WHERE clause injection
- Spring Boot auto-configuration via `MybatisPermissionAutoConfiguration`
- Sample application (`sample-basic`) with H2 in-memory database
- 4 built-in data scopes: `ALL`, `OWN_ORG`, `OWN_AND_CHILDREN`, `SELF`
