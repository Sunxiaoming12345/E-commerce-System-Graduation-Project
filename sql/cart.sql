CREATE TABLE `cart` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
    `product_id` bigint unsigned NOT NULL COMMENT '商品ID',
    `quantity` int NOT NULL DEFAULT '1' COMMENT '商品数量',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 插入购物车测试数据
INSERT INTO `cart` (`user_id`, `product_id`, `quantity`) VALUES
(1, 1, 1),
(1, 3, 2),
(2, 4, 1),
(3, 6, 1),
(4, 8, 3),
(5, 10, 1);
