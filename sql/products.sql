CREATE TABLE `products` (
                            `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品唯一ID',
                            `name` varchar(255) NOT NULL COMMENT '商品名称',
                            `description` text COMMENT '商品描述',
                            `image_url` varchar(500) DEFAULT NULL COMMENT '商品主图URL',
                            `category_id` bigint unsigned DEFAULT NULL COMMENT '商品分类ID（关联categories表）',
                            `price` decimal(10,2) NOT NULL COMMENT '商品价格',
                            `stock` int DEFAULT '0' COMMENT '库存数量',
                            `status` tinyint DEFAULT '1' COMMENT '商品状态（1:上架, 0:下架）',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表'