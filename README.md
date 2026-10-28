# spring-data-permission-starter

中文 | [English](README_EN.md)

**行级数据权限 Spring Boot Starter** —— 5 分钟接入，零业务代码侵入，SQL 层自动过滤。

## 为什么需要这个

做 B2B 系统时，你一定写过这样的代码：

```java
// ❌ 每个查询都要手动拼权限条件，散落在各处
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

**痛点：**
- 每写一个查询都要重复一遍权限逻辑，业务代码和权限代码纠缠
- 新增数据范围（如"本组织+下级"）要改所有 Service
- 单条记录的访问校验容易遗漏，留下安全漏洞
- 权限规则分散在几十个 Service 方法中，无法统一管理

**用这个 Starter 后：**

```java
// ✅ 业务代码只管查询，权限条件自动注入 SQL
public List<Order> listOrders(@CurrentPermission PermissionRequest request) {
    Map<String, Object> params = permissionHelper.createQuery(Order.class, request).getSqlParams();
    return orderMapper.selectWithScope(params);
}

// ✅ 单条校验一行代码，无权自动抛 AccessDeniedException
public Order getOrder(Long id, @CurrentPermission PermissionRequest request) {
    Order order = orderMapper.selectById(id);
    permissionHelper.checkAccess(request,
            DataPermissionTarget.of(order.getOrgId(), order.getOwnerUserId()));
    return order;
}
```

你只需实现一个 `PermissionResolver` 接口，告诉框架"这个用户能看到哪些组织的数据"，剩下的全部自动完成。

## 核心能力

| 能力 | 说明 |
|---|---|
| **列表过滤** | 自动在 SQL 中注入 `WHERE org_id IN (...)` 或 `WHERE owner_user_id = ?` |
| **单条校验** | 检查用户是否有权访问某条记录，无权抛 `AccessDeniedException` |
| **4 级标准范围** | `ALL` 全部 / `OWN_ORG` 本组织 / `OWN_AND_CHILDREN` 本组织+下级 / `SELF` 仅本人 |
| **失败关闭** | 组织范围为空时生成 `and 1=0`，绝不退化成“查全部” |
| **列名可配** | 全局配置 + `@DataPermissionTable` 实体级覆盖，无需改代码 |
| **零侵入** | 业务代码不引入任何权限注解或基类，只需注入 `DataPermissionHelper` |

## 和其他方案的对比

| | 手写 if-else | RuoYi 内置权限 | MyBatis-Plus DataScope | **本项目** |
|---|---|---|---|---|
| 代码侵入 | 每个方法都要写 | 需要整个平台 | 必须用 MyBatis-Plus | **零侵入，只加一行调用** |
| 新增数据范围 | 改所有 Service | 改平台代码 | 改 Provider | **改 PermissionResolver 一处** |
| 单条校验 | 容易遗漏 | 不支持 | 不支持 | **内置 checkAccess()** |
| SQL 透明度 | 拼接字符串 | 注解+拦截器 | 拦截器+ThreadLocal | **显式 `<include>`，可调试** |
| 适用场景 | 任何 | RuoYi 用户 | MyBatis-Plus 用户 | **任何 Spring Boot + MyBatis 项目** |

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>io.github.steventsai</groupId>
    <artifactId>spring-data-permission-spring-boot-starter</artifactId>
    <version>0.4.0</version>
</dependency>
```

### 2. 实现 PermissionResolver（唯一需要写的代码）

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
                .accessibleOrgIds(orgIds)        // OWN_ORG/OWN_AND_CHILDREN 必填
                .ownerUserId(request.operatorUserId()) // SELF 必填
                .build();
    }
}
```

> **注意**：Builder 会校验 scope 级别的必填字段，漏填会在构建时直接报错。

### 3. Controller 层

```java
@GetMapping("/orders")
public List<Order> listOrders(@CurrentPermission PermissionRequest request) {
    return orderService.listOrders(request);
}
```

> **生产环境提示**：基于 HTTP header `X-User-Id` 的解析器仅限演示，且从 0.4.0 起**默认关闭**。演示/测试需显式配置 `spring.data.permission.header-resolver-enabled: true`；生产环境应替换为 JWT / Session —— 继承 `PermissionArgumentResolver` 重写 `resolveArgument`，或提供自定义 `PermissionRequestFactory`。

> **返回码约定**：身份信息缺失或格式非法时，框架默认返回 `400 Bad Request`；身份已识别但无数据权限时，`AccessDeniedException` 默认标注为 `403 Forbidden`。示例工程为避免泄露记录是否存在，将其统一映射为 `404 Not Found`，业务可自行覆盖。

### 4. Service 层

```java
// 列表查询：自动注入权限条件
public List<Order> listOrders(PermissionRequest request) {
    Map<String, Object> params = permissionHelper.createQuery(Order.class, request).getSqlParams();
    return orderMapper.selectWithScope(params);
}

// 单条校验：无权自动抛 AccessDeniedException
public Order getOrder(Long id, PermissionRequest request) {
    Order order = orderMapper.selectById(id);
    permissionHelper.checkAccess(request,
            DataPermissionTarget.of(order.getOrgId(), order.getOwnerUserId()));
    return order;
}
```

### 5. Mapper 接口

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

## 配置属性

```yaml
spring:
  data:
    permission:
      enabled: true                        # 是否启用，默认 true
      header-resolver-enabled: false       # 是否注册 header 解析器，默认 false（演示/测试才开启）
      user-id-header: X-User-Id            # 提取用户 ID 的 HTTP header
      org-id-column: org_id                # 组织 ID 列名，默认 org_id
      owner-user-id-column: owner_user_id  # 拥有者用户 ID 列名，默认 owner_user_id
      max-org-ids: 1000                    # IN (...) 的最大组织数，0 表示不限制
```

如需按实体覆盖列名：

```java
@DataPermissionTable(orgIdColumn = "dept_id", ownerUserIdColumn = "creator_id")
public class Order { ... }
```

> 组织范围为空（`accessibleOrgIds` 为空列表）表示 **deny-all**，会生成 `and 1=0`，不会返回全部数据。需要“全部数据”请使用 `ALL` scope。
>
> `authorizedOrgIds`、`ownerUserId` 和 `_*` 前缀参数属于框架保留键，业务参数无法覆盖。

MyBatis 需要扫描框架的 SQL fragment：

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml,classpath:mybatis/*.xml
```

列名配置仅允许字母、数字、下划线，以及可选的表别名点号形式，例如 `org_id`、`t.org_id`。包含空格、引号、分号等非法字符会在启动时直接失败，避免错误配置进入 SQL 拼接链路。

## 架构设计

```
HTTP 请求 (可选 X-User-Id header，需显式开启)
    ↓
PermissionArgumentResolver → PermissionRequest
    ↓
PermissionResolver（你实现）→ DataPermissionContext（范围 + 组织ID + 用户ID）
    ↓
PermissionSqlParamAssembler → MyBatis 参数 Map
    ↓
Mapper XML <include ScopeCondition> → 自动注入 WHERE 条件
```

详见 [architecture/data-permission-model.md](https://github.com/StevenTsai/distribution-architecture/blob/main/architecture/data-permission-model.md)

## 示例项目

- [sample-basic](spring-data-permission-samples/sample-basic/) — 使用 H2 内存数据库的完整示例，含 ALL / OWN_ORG / OWN_AND_CHILDREN / SELF / deny-all 五种用户权限演示和集成测试

## 适用边界

- 适用于 `Spring Boot + MyBatis XML` 的显式 SQL 场景
- 当前设计强调“显式 `<include>` 注入权限条件”，不依赖拦截器或 ThreadLocal 魔法
- 非 Web 场景也可使用 `DataPermissionHelper`，但需要你自己构造 `PermissionRequest`
- 如果你使用自定义认证体系，建议替换默认 `PermissionArgumentResolver`；自定义 `PermissionRequest` 子类可通过 `PermissionRequestFactory` 注入
- 自定义 `DataScope` 默认按组织范围处理；若语义等同 ALL/SELF，可覆盖 `isAll()` / `isSelf()`
- 如果查询不是通过 XML `@Param("params") Map<String, Object>` 传参，本项目不会自动改写你的 SQL

## 常见问题

**Q: 启动报 `ScopeCondition not found`**
A: 检查 `mybatis.mapper-locations` 是否包含 `classpath:mybatis/*.xml`。

**Q: 报 `No qualifying bean of type 'DataPermissionHelper'`**
A: 必须提供一个 `PermissionResolver` 实现并标注 `@Component`。

**Q: 自定义列名不生效**
A: 确认 `spring.data.permission.org-id-column` 和 `owner-user-id-column` 配置正确，并确认 mapper 方法接受 `@Param("params") Map<String, Object>` 参数。

**Q: 为什么请求直接返回 400？**
A: 这表示身份信息未能解析成功，例如缺少 `X-User-Id` header 或用户 ID 不是数字；这和“已登录但无权限”的 403 是两类问题。

**Q: `@CurrentPermission` 参数一直是 null / 解析器不生效？**
A: 0.4.0 起 header 解析器默认关闭。演示或测试请设置 `spring.data.permission.header-resolver-enabled: true`；生产环境请注册自定义 `PermissionArgumentResolver`。

**Q: OWN_ORG scope 报 `accessibleOrgIds must not be null`**
A: `PermissionResolver.resolve()` 中必须为 OWN_ORG / OWN_AND_CHILDREN scope 设置 `accessibleOrgIds`。空列表是合法的 deny-all；`null` 才是配置遗漏。

**Q: 组织范围为空时为什么查到 0 条，而不是全部？**
A: 这是 0.4.0 起的失败关闭（fail-closed）设计：空列表生成 `and 1=0`。需要全部数据请显式使用 `ALL` scope。

**Q: 报 `accessibleOrgIds contains ... entries, which exceeds ... max-org-ids`？**
A: 单次 `IN (...)` 的组织数超过 `spring.data.permission.max-org-ids`（默认 1000）。请缩小范围、显式调大限制，或改用子查询方案。

**Q: `StandardDataScope.fromCode()` 抛 `IllegalArgumentException`？**
A: 0.4.0 起空白或未知 scope code 会快速失败，不再返回 `null`。请检查数据库中的 scope 值，统一为 `ALL` / `OWN_ORG` / `OWN_AND_CHILDREN` / `SELF`（大小写不敏感）。

## 从 0.3.x 升级

- header 解析器改为显式开启：`spring.data.permission.header-resolver-enabled=true`
- 未知或空白 scope code 现在抛异常
- 空组织列表语义由“不过滤”改为 deny-all
- 业务参数不能覆盖 `authorizedOrgIds` / `ownerUserId` / `_*` 保留键
- 实体类可用 `@DataPermissionTable` 覆盖列名
- 单条校验推荐使用 `DataPermissionTarget.of(orgId, ownerId)` 避免参数顺序错误

## License

[Apache License 2.0](LICENSE)
