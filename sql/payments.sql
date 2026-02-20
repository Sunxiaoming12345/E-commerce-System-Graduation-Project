CREATE TABLE `payments` (
                            `payment_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '支付ID',
                            `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
                            `payment_method` TINYINT NOT NULL COMMENT '支付方式',
                            `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
                            `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-支付失败',
                            `pay_time` DATETIME NULL COMMENT '支付时间',
                            FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
