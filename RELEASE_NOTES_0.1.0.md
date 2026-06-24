# Release Notes

## v0.1.0

`spring-data-permission-starter` 首个公开版本发布。

这是一个面向 `Spring Boot + MyBatis XML` 场景的行级数据权限 Starter，目标是把数据权限过滤下沉到 SQL 层，在尽量少侵入业务代码的前提下，统一处理列表过滤和单条访问校验。

## Highlights

- 5 分钟接入的数据权限 Starter
- 零业务基类侵入，只需实现一个 `PermissionResolver`
- SQL 层显式注入权限条件，便于调试和排查
- 内置列表过滤和单条记录访问校验
- 提供完整 sample、单元测试、集成测试和自动配置测试

## Included In This Release

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
- SQL-side list filtering
- Single-record access check
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

This version is designed for:

- `Spring Boot 3.x`
- `Java 17+`
- `MyBatis XML` explicit SQL projects

## Design Characteristics

- Explicit `<include>`-based permission injection
- No interceptor black-box SQL rewriting
- No ThreadLocal-dependent permission propagation
- Can be used in non-web scenarios by constructing `PermissionRequest` directly

## Configuration Reminder

Make sure MyBatis scans the framework SQL fragment:

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml,classpath:mybatis/*.xml
```

Default identity resolution reads from the `X-User-Id` HTTP header. For production systems, it is recommended to replace the default resolver with your own JWT / Session based implementation.

## Current Boundaries

- This release is primarily intended for `Spring Boot + MyBatis XML`
- It does not rewrite non-XML query paths automatically
- It expects mapper XML to receive `@Param("params") Map<String, Object>`
- This is the first public release and should be treated as a `0.x` version

## Feedback Welcome

Issues and pull requests are welcome, especially around:

- More permission scope models
- More real-world integration samples
- Better documentation and onboarding
- Production feedback from actual business systems
