CREATE TABLE `balance` (
                        `id` int NOT NULL AUTO_INCREMENT COMMENT '余额ID',
                        `user_id` int NOT NULL COMMENT '用户ID',
                        `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
                        `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `user_id` (`user_id`),
                        CONSTRAINT `balance_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
