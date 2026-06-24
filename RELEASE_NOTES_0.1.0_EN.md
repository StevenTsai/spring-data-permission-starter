# Release Notes

## v0.1.0

This is the first public release of `spring-data-permission-starter`.

`spring-data-permission-starter` is a row-level data permission starter for `Spring Boot + MyBatis XML`. Its goal is to push permission filtering down to the SQL layer, so teams can handle list filtering and single-record access checks consistently without scattering permission logic across business services.

## Highlights

- 5-minute integration for row-level data permission
- Zero business base-class intrusion, only one `PermissionResolver` to implement
- Explicit SQL-side permission injection for easier debugging
- Built-in list filtering and single-record access checks
- Includes sample app, unit tests, integration tests, and auto-configuration tests

## Included in This Release

### Core abstractions

- `PermissionRequest`
- `PermissionResolver`
- `DataPermissionContext`
- `DataPermissionHelper`
- `PermissionQueryBuilder`
- `AccessDeniedException`
- `MissingPermissionException`

### Built-in data scopes

- `ALL`
- `OWN_ORG`
- `OWN_AND_CHILDREN`
- `SELF`

### MyBatis integration

- Built-in `ScopeCondition` SQL fragment
- Permission parameter assembly
- SQL-layer list filtering
- Single-record access checks
- Configurable org/owner column names

### Spring MVC / Spring Boot support

- `@CurrentPermission`
- Default `PermissionArgumentResolver`
- Spring Boot auto-configuration
- Configurable user ID header
- Default `400 Bad Request` for invalid identity input
- Default `403 Forbidden` for access denied

### Sample and testing

- `sample-basic` H2 sample application
- Unit tests
- Integration tests
- Auto-configuration tests
- Build-time quality gates with `enforcer`, `surefire`, `jacoco`, and `checkstyle`

## Supported Scenario

This release is designed for:

- `Spring Boot 3.x`
- `Java 17+`
- `MyBatis XML` explicit SQL projects

## Design Characteristics

- Explicit `<include>`-based permission injection
- No interceptor-based black-box SQL rewriting
- No ThreadLocal-dependent permission propagation
- Can also be used in non-web scenarios by constructing `PermissionRequest` directly

## Configuration Reminder

Make sure MyBatis scans the framework SQL fragment:

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml,classpath:mybatis/*.xml
```

By default, identity resolution reads from the `X-User-Id` HTTP header. For production systems, it is recommended to replace the default resolver with your own JWT / Session based implementation.

## Current Boundaries

- This release primarily targets `Spring Boot + MyBatis XML`
- It does not automatically rewrite non-XML query paths
- It expects mapper XML to receive `@Param("params") Map<String, Object>`
- This is the first public release and should be treated as a `0.x` version

## Feedback Welcome

Issues and pull requests are welcome, especially around:

- More permission scope models
- More real-world integration samples
- Better documentation and onboarding
- Production feedback from real business systems
