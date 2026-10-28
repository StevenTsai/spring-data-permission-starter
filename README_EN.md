# spring-data-permission-starter

[中文](README.md) | English

**Row-level data permission Spring Boot Starter** — 5-minute integration, zero business code intrusion, automatic SQL filtering.

## Why You Need This

If you're building a B2B system, you've probably written code like this:

```java
// ❌ Every query needs manual permission logic, scattered across your codebase
public List<Order> listOrders(User user) {
    if (user.isAdmin()) {
        return orderMapper.selectAll();
    } else if (user.isManager()) {
        return orderMapper.selectByOrgIds(getOrgTree(user.getOrgId()));
    } else {
        return orderMapper.selectByOwner(user.getId());
    }
}
```

**The pain:**
- Every new query duplicates permission logic — business code tangled with access control
- Adding a new data scope (e.g. "own org + children") means editing every Service
- Single-record access checks are easy to forget, leaving security holes
- Permission rules scattered across dozens of Service methods, impossible to audit

**With this Starter:**

```java
// ✅ Business code only handles the query — permission conditions auto-injected into SQL
public List<Order> listOrders(@CurrentPermission PermissionRequest request) {
    Map<String, Object> params = permissionHelper.createQuery(Order.class, request).getSqlParams();
    return orderMapper.selectWithScope(params);
}

// ✅ Single-record check in one line — unauthorized access auto-throws AccessDeniedException
public Order getOrder(Long id, @CurrentPermission PermissionRequest request) {
    Order order = orderMapper.selectById(id);
    permissionHelper.checkAccess(request,
            DataPermissionTarget.of(order.getOrgId(), order.getOwnerUserId()));
    return order;
}
```

You only implement one interface — `PermissionResolver` — to tell the framework "what data can this user see". Everything else is automatic.

## Core Capabilities

| Capability | Description |
|---|---|
| **List filtering** | Auto-injects `WHERE org_id IN (...)` or `WHERE owner_user_id = ?` into SQL |
| **Single-record check** | Validates access to a specific record, throws `AccessDeniedException` if denied |
| **4 standard scopes** | `ALL` / `OWN_ORG` / `OWN_AND_CHILDREN` / `SELF` |
| **Fail-closed** | An empty org scope emits `and 1=0` instead of returning every row |
| **Configurable columns** | Global config plus entity-level `@DataPermissionTable` overrides |
| **Zero intrusion** | No permission annotations or base classes in business code |

## Comparison with Alternatives

| | Hand-rolled if-else | RuoYi built-in | MyBatis-Plus DataScope | **This project** |
|---|---|---|---|---|
| Code intrusion | Every method | Requires entire platform | Must use MyBatis-Plus | **Zero — one method call** |
| New data scope | Edit all Services | Edit platform code | Edit Provider | **Edit PermissionResolver in one place** |
| Single-record check | Easy to miss | Not supported | Not supported | **Built-in checkAccess()** |
| SQL transparency | String concatenation | Annotation + interceptor | Interceptor + ThreadLocal | **Explicit `<include>`, debuggable** |
| Use case | Any | RuoYi users | MyBatis-Plus users | **Any Spring Boot + MyBatis project** |

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>io.github.steventsai</groupId>
    <artifactId>spring-data-permission-spring-boot-starter</artifactId>
    <version>0.4.0</version>
</dependency>
```

### 2. Implement PermissionResolver (the only code you write)

```java
@Component
public class MyPermissionResolver implements PermissionResolver {

    @Autowired
    private UserMapper userMapper;

    @Override
    public DataPermissionContext resolve(PermissionRequest request) {
        User user = userMapper.selectById(request.operatorUserId());
        List<Long> orgIds = resolveOrgIds(user.getOrgId(), user.getDataScope());

        return DataPermissionContext.builder()
                .operatorUserId(request.operatorUserId())
                .scope(StandardDataScope.fromCode(user.getDataScope()))
                .accessibleOrgIds(orgIds)        // Required for OWN_ORG / OWN_AND_CHILDREN
                .ownerUserId(request.operatorUserId()) // Required for SELF
                .build();
    }
}
```

> **Note**: The Builder validates scope-specific invariants. Missing required fields will fail at construction time with a clear error message.

### 3. Controller Layer

```java
@GetMapping("/orders")
public List<Order> listOrders(@CurrentPermission PermissionRequest request) {
    return orderService.listOrders(request);
}
```

> **Production note**: The `X-User-Id` header resolver is demo-only and is **disabled by default** since 0.4.0. For demos/tests set `spring.data.permission.header-resolver-enabled: true`. In production, replace it with JWT / Session by extending `PermissionArgumentResolver`, or provide a custom `PermissionRequestFactory`.

> **Status code contract**: Missing or malformed identity information returns `400 Bad Request`; identified users without data access receive `403 Forbidden` by default (`AccessDeniedException` carries `@ResponseStatus(FORBIDDEN)`). The sample maps denials to `404 Not Found` to avoid leaking record existence; applications may override this.

### 4. Service Layer

```java
// List query: permission conditions auto-injected
public List<Order> listOrders(PermissionRequest request) {
    Map<String, Object> params = permissionHelper.createQuery(Order.class, request).getSqlParams();
    return orderMapper.selectWithScope(params);
}

// Single-record check: auto-throws AccessDeniedException
public Order getOrder(Long id, PermissionRequest request) {
    Order order = orderMapper.selectById(id);
    permissionHelper.checkAccess(request,
            DataPermissionTarget.of(order.getOrgId(), order.getOwnerUserId()));
    return order;
}
```

### 5. Mapper Interface

```java
List<Order> selectWithScope(@Param("params") Map<String, Object> params);
```

### 6. Mapper XML

```xml
<select id="selectWithScope" resultMap="BaseResultMap">
    SELECT * FROM biz_order
    WHERE 1=1
    <include refid="io.github.steventsai.permission.ScopeCondition"/>
</select>
```

## Configuration

```yaml
spring:
  data:
    permission:
      enabled: true                        # Enable/disable, default true
      header-resolver-enabled: false       # Register the header resolver, default false (demos/tests only)
      user-id-header: X-User-Id            # HTTP header for user ID extraction
      org-id-column: org_id                # Org ID column name, default org_id
      owner-user-id-column: owner_user_id  # Owner user ID column name, default owner_user_id
      max-org-ids: 1000                    # Max org IDs in IN (...), 0 = unlimited
```

To override columns per entity:

```java
@DataPermissionTable(orgIdColumn = "dept_id", ownerUserIdColumn = "creator_id")
public class Order { ... }
```

> An empty organization scope (`accessibleOrgIds` is an empty list) means **deny-all** and emits `and 1=0`; it never returns every row. Use `ALL` scope for unrestricted access.
>
> `authorizedOrgIds`, `ownerUserId`, and `_*`-prefixed keys are reserved and cannot be overridden by business parameters.

MyBatis needs to scan the framework's SQL fragment:

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml,classpath:mybatis/*.xml
```

Column configuration only accepts letters, digits, underscores, and optional table-alias dot notation such as `org_id` or `t.org_id`. Invalid characters like spaces, quotes, or semicolons will fail fast at startup to avoid unsafe SQL substitution.

## Architecture

```
HTTP Request (opt-in X-User-Id header)
    ↓
PermissionArgumentResolver → PermissionRequest
    ↓
PermissionResolver (you implement) → DataPermissionContext (scope + orgIds + userId)
    ↓
PermissionSqlParamAssembler → MyBatis parameter Map
    ↓
Mapper XML <include ScopeCondition> → auto-inject WHERE clause
```

See [architecture/data-permission-model.md](https://github.com/StevenTsai/distribution-architecture/blob/main/architecture/data-permission-model.md)

## Sample Projects

- [sample-basic](spring-data-permission-samples/sample-basic/) — Complete example with H2 in-memory database, five user permission levels (including deny-all), and integration tests

## Scope and Boundaries

- Designed for explicit SQL with `Spring Boot + MyBatis XML`
- The project intentionally uses explicit `<include>`-based permission injection instead of interceptors or ThreadLocal-based rewriting
- Non-web scenarios are supported too, as long as you construct `PermissionRequest` yourself
- If your authentication model is custom, replace the default `PermissionArgumentResolver`; custom `PermissionRequest` subtypes can be injected through `PermissionRequestFactory`
- Custom `DataScope` implementations default to organization scope; override `isAll()` / `isSelf()` when their semantics match those modes
- If your query path does not pass `@Param("params") Map<String, Object>` into mapper XML, this project will not rewrite SQL for you

## Troubleshooting

**Q: Startup error `ScopeCondition not found`**
A: Check that `mybatis.mapper-locations` includes `classpath:mybatis/*.xml`.

**Q: Error `No qualifying bean of type 'DataPermissionHelper'`**
A: You must provide a `PermissionResolver` implementation annotated with `@Component`.

**Q: Custom column names not taking effect**
A: Verify `spring.data.permission.org-id-column` / `owner-user-id-column` are configured, and your mapper method accepts `@Param("params") Map<String, Object>`.

**Q: Why am I getting 400 instead of 403?**
A: `400 Bad Request` means identity resolution failed, for example the `X-User-Id` header is missing or malformed. `403 Forbidden` means the user was identified successfully but does not have access to the requested data.

**Q: `@CurrentPermission` is null / the resolver is not active?**
A: Since 0.4.0 the header resolver is disabled by default. Set `spring.data.permission.header-resolver-enabled: true` for demos/tests, or register a custom `PermissionArgumentResolver` in production.

**Q: OWN_ORG scope throws `accessibleOrgIds must not be null`**
A: `PermissionResolver.resolve()` must set `accessibleOrgIds` for OWN_ORG / OWN_AND_CHILDREN scopes. An empty list is valid deny-all; only `null` is a configuration error.

**Q: Why does an empty org scope return 0 rows instead of all rows?**
A: Fail-closed behavior introduced in 0.4.0: an empty list emits `and 1=0`. Use `ALL` scope when unrestricted access is intended.

**Q: `accessibleOrgIds ... exceeds ... max-org-ids`?**
A: A single `IN (...)` exceeded `spring.data.permission.max-org-ids` (default 1000). Narrow the scope, raise the limit explicitly, or switch to a subquery-based design.

**Q: `StandardDataScope.fromCode()` throws `IllegalArgumentException`?**
A: Since 0.4.0 blank or unknown scope codes fail fast instead of returning `null`. Normalize stored values to `ALL` / `OWN_ORG` / `OWN_AND_CHILDREN` / `SELF` (case-insensitive).

## Upgrading from 0.3.x

- The header resolver is now opt-in: set `spring.data.permission.header-resolver-enabled=true`
- Unknown or blank scope codes now throw
- An empty org list now means deny-all instead of "no filter"
- Business parameters cannot override the reserved `authorizedOrgIds` / `ownerUserId` / `_*` keys
- Entities can override column names with `@DataPermissionTable`
- Prefer `DataPermissionTarget.of(orgId, ownerId)` for single-record checks to avoid argument-order mistakes

## License

[Apache License 2.0](LICENSE)
