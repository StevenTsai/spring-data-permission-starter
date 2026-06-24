-- Sample data for demonstrating data permission filtering
-- org 1 has child org 2
-- User 1 (admin, ALL), User 2 (org 1 manager), User 3 (org 1, with children), User 4 (org 2 staff)

INSERT INTO biz_order (order_no, customer_name, status, org_id, owner_user_id) VALUES
('ORD-001', 'Alice Corp',   'active', 1, 1),
('ORD-002', 'Bob Inc',      'active', 1, 2),
('ORD-003', 'Charlie Ltd',  'active', 1, 3),
('ORD-004', 'Delta Co',     'active', 2, 4),
('ORD-005', 'Echo LLC',     'active', 2, 4),
('ORD-006', 'Foxtrot GmbH', 'closed', 3, 1);
