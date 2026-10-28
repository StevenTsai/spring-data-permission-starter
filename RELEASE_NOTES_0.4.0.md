# Release Notes

## v0.4.0

`spring-data-permission-starter` 0.4.0 是一次以**失败关闭（fail-closed）**和**接入安全性**为核心的版本。

本版本重点修复了“空组织范围退化为查全部”的风险，收敛了默认开启的 header 身份解析，并为实体级列名覆盖、自定义请求类型和大组织树场景补充了能力。

## Highlights

- 空组织范围默认 deny-all，SQL 生成 `and 1=0`，不会再意外返回全部数据
- header 身份解析器改为显式开启，避免生产环境被 `X-User-Id` 伪造
- 新增 `@DataPermissionTable`，按实体覆盖组织/拥有者列名
- 新增 `PermissionRequestFactory`，支持自定义 `PermissionRequest` 子类
- 新增 `DataPermissionTarget`，避免单条校验传参顺序错误
- 新增 `max-org-ids` 限制，防止超大 `IN (...)` 拖垮数据库
- 权限参数设为保留键，业务参数无法覆盖或移除过滤条件

## Included In This Release

### Core

- `DataPermissionTarget` 记录类型与 `checkAccess(request, target)` 重载
- 自定义 `DataScope` 可通过 `isAll()` / `isSelf()` 复用内置 SQL 片段
- `DataPermissionContext` 对不可变列表、重复 ID、空列表和 null 元素的明确处理
- `AccessDeniedException` 标注 `@ResponseStatus(FORBIDDEN)`

### MyBatis

- `@DataPermissionTable` 实体级列名覆盖
- `PermissionSqlParamAssembler` 新增 `max-org-ids` 校验
- `PermissionQueryBuilder` 参数保留键保护与不可变快照
- `ScopeCondition` 使用显式 `_allScope` / `_selfScope` / `_denyAll` 标志

### Spring MVC / Spring Boot

- 新增 `PermissionWebAutoConfiguration`
- `header-resolver-enabled` 默认 `false`
- 新增 `PermissionRequestFactory` 扩展点
- 自动配置拆分为核心配置与可选 Web 配置

### Sample and testing

- sample 增加 deny-all 用户与未知用户场景
- 单条无权访问统一返回 404（防枚举）
- 新增工厂、Web 自动配置、实体列名覆盖、deny-all、参数保留键等测试
- CI 覆盖 sample 模块

## Breaking Changes

- header 解析器不再默认注册，需要设置 `spring.data.permission.header-resolver-enabled=true`
- `StandardDataScope.fromCode()` 对空白/未知值改为抛 `IllegalArgumentException`
- 空 `accessibleOrgIds` 由“不过滤”改为 deny-all
- `authorizedOrgIds`、`ownerUserId`、`_*` 参数成为保留键，业务参数不能覆盖

## Upgrading from 0.3.x

1. 如仍使用演示用 header 解析器，显式开启 `spring.data.permission.header-resolver-enabled=true`
2. 检查数据库中 `DataScope` 存储值，未知值现在会快速失败
3. 检查 `PermissionResolver`：组织范围为空时现在表示无权限，而不是全部数据
4. 检查业务参数 Map，避免使用框架保留键
5. 单条校验建议改用 `DataPermissionTarget.of(orgId, ownerId)`

## Verification

- `mvn clean verify -B -Pwith-samples` 通过
- core / mybatis 模块 JaCoCo 行覆盖率均不低于 80%
- Checkstyle 零违规

## Current Boundaries

- 仍面向 `Spring Boot + MyBatis XML` 显式 SQL 场景
- 不自动改写非 XML 查询路径
- mapper XML 仍需接收 `@Param("params") Map<String, Object>`

## Feedback Welcome

欢迎反馈组织树规模、多租户扩展、自定义 scope 和真实生产接入中的问题。
