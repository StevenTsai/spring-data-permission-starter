package com.github.steventsai.permission.sample.entity;

import lombok.Data;

/**
 * Sample business entity with org_id and owner_user_id for data permission filtering.
 */
@Data
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
}
