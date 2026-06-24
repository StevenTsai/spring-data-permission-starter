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
    permissionHelper.checkAccess(request, order.getOrgId(), order.getOwnerUserId());
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
| **列名可配** | 组织 ID 列名、拥有者列名通过配置文件自定义，无需改代码 |
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
    <groupId>com.github.steventsai</groupId>
    <artifactId>spring-data-permission-spring-boot-starter</artifactId>
    <version>0.1.0</version>
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

> **生产环境提示**：默认从 HTTP header `X-User-Id` 提取用户 ID，仅限演示。生产环境应替换为 JWT / Session —— 继承 `PermissionArgumentResolver` 重写 `resolveArgument` 即可。

> **返回码约定**：身份信息缺失或格式非法时，框架默认返回 `400 Bad Request`；身份已识别但无数据权限时，返回 `403 Forbidden`。

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
    permissionHelper.checkAccess(request, order.getOrgId(), order.getOwnerUserId());
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
    <include refid="com.github.steventsai.permission.ScopeCondition"/>
</select>
```

## 配置属性

```yaml
spring:
  data:
    permission:
      enabled: true                        # 是否启用，默认 true
      user-id-header: X-User-Id            # 提取用户 ID 的 HTTP header
      org-id-column: org_id                # 组织 ID 列名，默认 org_id
      owner-user-id-column: owner_user_id  # 拥有者用户 ID 列名，默认 owner_user_id
```

MyBatis 需要扫描框架的 SQL fragment：

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml,classpath:mybatis/*.xml
```

列名配置仅允许字母、数字、下划线，以及可选的表别名点号形式，例如 `org_id`、`t.org_id`。包含空格、引号、分号等非法字符会在启动时直接失败，避免错误配置进入 SQL 拼接链路。

## 架构设计

```
HTTP 请求 (X-User-Id header)
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

- [sample-basic](spring-data-permission-samples/sample-basic/) — 使用 H2 内存数据库的完整示例，含 4 种用户权限演示和集成测试

## 适用边界

- 适用于 `Spring Boot + MyBatis XML` 的显式 SQL 场景
- 当前设计强调“显式 `<include>` 注入权限条件”，不依赖拦截器或 ThreadLocal 魔法
- 非 Web 场景也可使用 `DataPermissionHelper`，但需要你自己构造 `PermissionRequest`
- 如果你使用自定义认证体系，建议替换默认 `PermissionArgumentResolver`
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

**Q: OWN_ORG scope 报 `accessibleOrgIds must not be null`**
A: `PermissionResolver.resolve()` 中必须为 OWN_ORG / OWN_AND_CHILDREN scope 设置 `accessibleOrgIds`。

## License

[Apache License 2.0](LICENSE)
