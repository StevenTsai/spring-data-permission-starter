package io.github.steventsai.permission.sample.mapper;

import io.github.steventsai.permission.sample.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper demonstrating permission-scoped queries.
 */
@Mapper
public interface OrderMapper {

    /**
     * List orders with permission filtering.
     * The params map is produced by PermissionQueryBuilder.getSqlParams() and contains
     * authorizedOrgIds, ownerUserId, scope flags, and validated column names.
     */
    List<Order> selectWithScope(@Param("params") Map<String, Object> params);

    /**
     * Load one row by ID. Callers must invoke DataPermissionHelper.checkAccess
     * before returning it to a user.
     */
    Order selectById(@Param("id") Long id);

    void insert(Order order);
}
