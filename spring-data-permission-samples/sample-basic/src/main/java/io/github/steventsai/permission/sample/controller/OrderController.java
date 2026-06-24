package io.github.steventsai.permission.sample.controller;

import io.github.steventsai.permission.PermissionRequest;
import io.github.steventsai.permission.mybatis.CurrentPermission;
import io.github.steventsai.permission.sample.entity.Order;
import io.github.steventsai.permission.sample.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller demonstrating @CurrentPermission annotation usage.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * List orders with permission filtering.
     * <p>
     * The @CurrentPermission annotation automatically resolves the PermissionRequest
     * from the HTTP request header "X-User-Id".
     *
     * <h3>Test with curl:</h3>
     * <pre>
     * # User 1 (admin, ALL scope) - sees all orders
     * curl -H "X-User-Id: 1" http://localhost:8080/api/orders
     *
     * # User 2 (manager, OWN_ORG scope) - sees org 1 orders only
     * curl -H "X-User-Id: 2" http://localhost:8080/api/orders
     *
     * # User 4 (staff, SELF scope) - sees own orders only
     * curl -H "X-User-Id: 4" http://localhost:8080/api/orders
     * </pre>
     */
    @GetMapping
    public List<Order> listOrders(@CurrentPermission PermissionRequest request) {
        return orderService.listOrders(request);
    }

    /**
     * Get a single order with access check.
     * <p>
     * Returns 403 if the user doesn't have permission to access this order.
     */
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable("id") Long id,
                          @CurrentPermission PermissionRequest request) {
        return orderService.getOrder(id, request);
    }

    /**
     * List all orders without permission filtering (admin use).
     */
    @GetMapping("/all")
    public List<Order> listAll() {
        return orderService.listAllOrders();
    }
}
