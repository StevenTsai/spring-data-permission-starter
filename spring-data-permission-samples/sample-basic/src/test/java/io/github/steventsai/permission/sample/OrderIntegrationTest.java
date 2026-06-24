package io.github.steventsai.permission.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the sample-basic application.
 * <p>
 * Uses H2 in-memory database with pre-loaded data (schema.sql + data.sql).
 * <p>
 * Test data: 6 orders across 3 orgs (org 1: 3 orders, org 2: 2 orders, org 3: 1 order).
 * <p>
 * Test users:
 * - User 1 (admin): ALL scope — sees all 6 orders
 * - User 2 (manager): OWN_ORG scope, org 1 — sees 3 orders
 * - User 3 (manager): OWN_AND_CHILDREN scope, org 1 (children: org 2) — sees 5 orders
 * - User 4 (staff): SELF scope — sees 2 own orders
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // --- List orders with permission filtering ---

    @Test
    void listOrders_admin_allScope_seesAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void listOrders_manager_ownOrgScope_seesOrg1Orders() throws Exception {
        // Org 1 has 3 orders: ORD-001, ORD-002, ORD-003
        mockMvc.perform(get("/api/orders").header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].orgId").value(1))
                .andExpect(jsonPath("$[1].orgId").value(1))
                .andExpect(jsonPath("$[2].orgId").value(1));
    }

    @Test
    void listOrders_manager_ownAndChildrenScope_seesOrg1And2Orders() throws Exception {
        // Org 1 (3 orders) + Org 2 (2 orders) = 5
        mockMvc.perform(get("/api/orders").header("X-User-Id", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void listOrders_staff_selfScope_seesOwnOrders() throws Exception {
        mockMvc.perform(get("/api/orders").header("X-User-Id", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ownerUserId").value(4))
                .andExpect(jsonPath("$[1].ownerUserId").value(4));
    }

    @Test
    void listOrders_missingUserIdHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Missing user ID")));
    }

    @Test
    void listOrders_invalidUserIdHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders").header("X-User-Id", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid user ID")));
    }

    // --- Get single order with access check ---

    @Test
    void getOrder_admin_canAccessAnyOrder() throws Exception {
        mockMvc.perform(get("/api/orders/1").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrder_manager_canAccessOwnOrgOrder() throws Exception {
        // User 2 has OWN_ORG scope for org 1, order 1 belongs to org 1
        mockMvc.perform(get("/api/orders/1").header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrder_manager_cannotAccessOtherOrgOrder() throws Exception {
        // User 2 has OWN_ORG scope for org 1, order 4 belongs to org 2
        mockMvc.perform(get("/api/orders/4").header("X-User-Id", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrder_staff_canAccessOwnOrder() throws Exception {
        // User 4 has SELF scope, order 5 has ownerUserId=4
        mockMvc.perform(get("/api/orders/5").header("X-User-Id", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getOrder_staff_cannotAccessOtherOrder() throws Exception {
        // User 4 has SELF scope, order 1 has ownerUserId=1
        mockMvc.perform(get("/api/orders/1").header("X-User-Id", "4"))
                .andExpect(status().isForbidden());
    }

    // --- List all (no permission filtering) ---

    @Test
    void listAll_returnsAllOrdersRegardlessOfPermission() throws Exception {
        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

}
