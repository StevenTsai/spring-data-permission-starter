package com.github.steventsai.permission.sample.service;

import com.github.steventsai.permission.DataPermissionHelper;
import com.github.steventsai.permission.PermissionRequest;
import com.github.steventsai.permission.sample.entity.Order;
import com.github.steventsai.permission.sample.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service demonstrating data permission usage patterns.
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final DataPermissionHelper permissionHelper;

    public OrderService(OrderMapper orderMapper, DataPermissionHelper permissionHelper) {
        this.orderMapper = orderMapper;
        this.permissionHelper = permissionHelper;
    }

    /**
     * List orders with automatic permission filtering.
     * <p>
     * Uses PermissionQueryBuilder to assemble permission parameters,
     * then passes the full params map to the mapper.
     */
    public List<Order> listOrders(PermissionRequest request) {
        Map<String, Object> params = permissionHelper.createQuery(Order.class, request).getSqlParams();
        return orderMapper.selectWithScope(params);
    }

    /**
     * Get a single order with access check.
     * <p>
     * Throws AccessDeniedException if the user doesn't have permission.
     */
    public Order getOrder(Long id, PermissionRequest request) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }

        // Check access: throws AccessDeniedException if denied
        permissionHelper.checkAccess(request, order.getOrgId(), order.getOwnerUserId());

        return order;
    }

    /**
     * List all orders without permission filtering (admin use).
     */
    public List<Order> listAllOrders() {
        return orderMapper.selectAll();
    }
}
