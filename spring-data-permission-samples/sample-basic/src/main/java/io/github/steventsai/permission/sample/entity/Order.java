package io.github.steventsai.permission.sample.entity;

import io.github.steventsai.permission.DataPermissionTable;

/**
 * Sample business entity with org_id and owner_user_id for data permission filtering.
 * <p>
 * The annotation is optional; it demonstrates entity-level column overrides.
 */
@DataPermissionTable(orgIdColumn = "org_id", ownerUserIdColumn = "owner_user_id")
public class Order {

    private Long id;
    private String orderNo;
    private String customerName;
    private String status;

    /**
     * Organization ID this order belongs to.
     * Used by ScopeCondition for org-level filtering.
     */
    private Long orgId;

    /**
     * User ID of the order creator/owner.
     * Used by ScopeCondition for SELF scope filtering.
     */
    private Long ownerUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}
