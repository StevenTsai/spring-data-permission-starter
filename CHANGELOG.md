# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).

## [0.1.0-SNAPSHOT] - Unreleased

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
