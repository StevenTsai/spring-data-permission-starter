package com.github.steventsai.permission.sample.mapper;

import com.github.steventsai.permission.sample.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper demonstrating dual query pattern:
 * - selectAll: no permission filtering (admin/internal use)
 * - selectWithScope: with permission filtering (user-facing)
 */
@Mapper
public interface OrderMapper {

    /**
     * List all orders without permission filtering.
     */
    List<Order> selectAll();

    /**
     * List orders with permission filtering.
     * The params map is produced by PermissionQueryBuilder.getSqlParams() and contains
     * authorizedOrgIds, ownerUserId, _orgIdColumn, _ownerUserIdColumn, etc.
     */
    List<Order> selectWithScope(@Param("params") Map<String, Object> params);

    /**
     * Count orders with permission filtering.
     */
    long countWithScope(@Param("params") Map<String, Object> params);

    Order selectById(@Param("id") Long id);

    void insert(Order order);
}
