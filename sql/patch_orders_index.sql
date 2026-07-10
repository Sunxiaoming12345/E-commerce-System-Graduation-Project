-- orders 表性能索引
-- 用户查询自己的订单列表（最频繁）
ALTER TABLE orders ADD INDEX idx_user_status (user_id, order_status);
-- 按订单号查询（幂等校验、支付等场景）
ALTER TABLE orders ADD INDEX idx_order_number (order_number);
-- 管理端按状态统计
ALTER TABLE orders ADD INDEX idx_status (order_status);
