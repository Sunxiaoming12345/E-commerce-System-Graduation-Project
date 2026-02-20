CREATE TABLE `orders` (
                          `order_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
                          `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
                          `order_number` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
                          `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待付款，1-已付款，2-已发货，3-已完成，4-已取消',
                          `payment_method` TINYINT NOT NULL DEFAULT 0 COMMENT '支付方式：0-支付宝，1-微信，2-银行卡',
                          `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
                          `shipping_address` TEXT NOT NULL COMMENT '收货地址',
                          `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
                          `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
                          `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `pay_time` DATETIME NULL COMMENT '支付时间',
                          `ship_time` DATETIME NULL COMMENT '发货时间',
                          `complete_time` DATETIME NULL COMMENT '完成时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
