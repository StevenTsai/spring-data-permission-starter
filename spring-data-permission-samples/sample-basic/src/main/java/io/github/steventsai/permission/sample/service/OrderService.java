package io.github.steventsai.permission.sample.service;

import io.github.steventsai.permission.DataPermissionHelper;
import io.github.steventsai.permission.DataPermissionTarget;
import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.sample.entity.Order;
import io.github.steventsai.permission.sample.exception.OrderNotFoundException;
import io.github.steventsai.permission.sample.mapper.OrderMapper;
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
     * Missing and unauthorized records both produce 404 in this sample to avoid
     * leaking the existence of records the caller cannot access.
     */
    public Order getOrder(Long id, PermissionRequest request) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new OrderNotFoundException();
        }

        permissionHelper.checkAccess(request,
                new DataPermissionTarget(order.getOrgId(), order.getOwnerUserId()));

        return order;
    }
}
