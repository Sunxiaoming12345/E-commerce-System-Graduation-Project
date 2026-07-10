-- ============================================================
-- MySQL 容器初始化脚本
-- 首次启动时自动执行，创建表结构 + 测试数据
-- ============================================================

SET NAMES utf8mb4;

-- ==================== 管理员表 ====================
CREATE TABLE IF NOT EXISTS `admin` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `name` varchar(50) NOT NULL COMMENT '姓名',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==================== 用户表 ====================
CREATE TABLE IF NOT EXISTS `user` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `name` varchar(50) NOT NULL COMMENT '姓名',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `default_receiver` varchar(50) DEFAULT NULL COMMENT '常用收货人',
    `default_phone` varchar(20) DEFAULT NULL COMMENT '常用联系电话',
    `default_address` text DEFAULT NULL COMMENT '常用收货地址',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==================== 余额表 ====================
CREATE TABLE IF NOT EXISTS `balance` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '余额ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_id` (`user_id`),
    CONSTRAINT `balance_user_id_foreign` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ==================== 分类表 ====================
CREATE TABLE IF NOT EXISTS `categories` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
    `sort` int DEFAULT '0' COMMENT '排序权重',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- ==================== 商品表 ====================
CREATE TABLE IF NOT EXISTS `products` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '商品唯一ID',
    `name` varchar(255) NOT NULL COMMENT '商品名称',
    `description` text COMMENT '商品描述',
    `image_url` varchar(500) DEFAULT NULL COMMENT '商品主图URL',
    `category_id` bigint unsigned DEFAULT NULL COMMENT '商品分类ID（关联categories表）',
    `price` decimal(10,2) NOT NULL COMMENT '商品价格',
    `stock` int DEFAULT '0' COMMENT '库存数量',
    `status` tinyint DEFAULT '1' COMMENT '商品状态（1:上架, 0:下架）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ==================== 购物车表 ====================
CREATE TABLE IF NOT EXISTS `cart` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` int NOT NULL COMMENT '用户ID',
    `product_id` bigint unsigned NOT NULL COMMENT '商品ID',
    `quantity` int NOT NULL DEFAULT '1' COMMENT '商品数量',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ==================== 订单表 ====================
CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `order_number` VARCHAR(48) NOT NULL UNIQUE COMMENT '订单编号',
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

-- ==================== 订单商品表 ====================
CREATE TABLE IF NOT EXISTS `order_items` (
    `item_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '订单商品ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_price` DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `subtotal` DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

-- ==================== 支付记录表 ====================
CREATE TABLE IF NOT EXISTS `payments` (
    `payment_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '支付ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `payment_method` TINYINT NOT NULL COMMENT '支付方式',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-支付失败',
    `pay_time` DATETIME NULL COMMENT '支付时间',
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ==================== 商品评价表 ====================
CREATE TABLE IF NOT EXISTS `reviews` (
    `review_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT UNSIGNED NULL COMMENT '订单ID（可空）',
    `rating` TINYINT NOT NULL COMMENT '评分 1-5',
    `content` TEXT COMMENT '评价内容',
    `images` VARCHAR(1000) COMMENT '图片URL（逗号分隔）',
    `status` TINYINT DEFAULT 1 COMMENT '1=显示 0=隐藏',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_reviews_product` (`product_id`),
    INDEX `idx_reviews_user` (`user_id`),
    INDEX `idx_reviews_status` (`status`),
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ==================== 优惠券表 ====================
CREATE TABLE IF NOT EXISTS `coupons` (
    `coupon_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT NOT NULL COMMENT '0=满减 1=折扣',
    `discount_value` DECIMAL(10,2) NOT NULL COMMENT '满减金额或折扣率',
    `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    `total_count` INT NOT NULL COMMENT '发放总量',
    `used_count` INT DEFAULT 0 COMMENT '已使用数量',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '1=启用 0=停用',
    `is_lottery` TINYINT DEFAULT 0 COMMENT '0=普通优惠券 1=抽奖专享',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_coupons_status` (`status`),
    INDEX `idx_coupons_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ==================== 用户优惠券表 ====================
CREATE TABLE IF NOT EXISTS `user_coupons` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT UNSIGNED NOT NULL COMMENT '优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '0=未使用 1=已使用 2=已过期',
    `order_id` BIGINT UNSIGNED NULL COMMENT '使用的订单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` DATETIME NULL COMMENT '使用时间',
    UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
    INDEX `idx_user_coupons_user` (`user_id`, `status`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`coupon_id`) REFERENCES `coupons`(`coupon_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ==================== 退款表 ====================
CREATE TABLE IF NOT EXISTS `refunds` (
    `refund_id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '退款ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    `reason` TEXT COMMENT '退款原因',
    `status` TINYINT DEFAULT 0 COMMENT '0=待处理 1=已通过 2=已拒绝 3=已完成',
    `admin_remark` TEXT COMMENT '管理员备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `refund_time` DATETIME NULL COMMENT '退款完成时间',
    INDEX `idx_refunds_user` (`user_id`),
    INDEX `idx_refunds_order` (`order_id`),
    INDEX `idx_refunds_status` (`status`),
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款表';


-- ==================== 客服聊天消息表 ====================
CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `sender_id` INT NOT NULL COMMENT '发送者ID',
    `sender_type` TINYINT NOT NULL COMMENT '0=用户 1=管理员',
    `product_id` BIGINT UNSIGNED NULL COMMENT '关联商品ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '0=未读 1=已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX `idx_sender` (`sender_id`, `sender_type`),
    INDEX `idx_product` (`product_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服聊天消息表';

-- ==================== 登录日志表 ====================
CREATE TABLE IF NOT EXISTS `login_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `ip` VARCHAR(50) NOT NULL COMMENT '登录IP',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录成功日志';

-- ==================== 秒杀商品表 ====================
CREATE TABLE IF NOT EXISTS `seckill_products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '秒杀活动ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称',
    `product_image` VARCHAR(500) DEFAULT '' COMMENT '商品主图',
    `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '秒杀库存',
    `origin_stock` INT NOT NULL DEFAULT 0 COMMENT '原始库存',
    `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
    `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '1=未开始 2=进行中 3=已结束',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_time` (`start_time`, `end_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- ==================== 抽奖配置表 ====================
CREATE TABLE IF NOT EXISTS `lottery_config` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    `daily_limit` INT DEFAULT 3 COMMENT '每日抽奖次数上限',
    `point_cost` INT DEFAULT 10 COMMENT '每次抽奖消耗积分',
    `status` TINYINT DEFAULT 1 COMMENT '1=启用 0=停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖配置表';

-- ==================== 奖池表 ====================
CREATE TABLE IF NOT EXISTS `lottery_pool` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '奖品ID',
    `prize_name` VARCHAR(100) NOT NULL COMMENT '奖品名称',
    `prize_type` TINYINT NOT NULL COMMENT '1=优惠券 2=积分 3=实物',
    `prize_value` DECIMAL(10,2) DEFAULT 0 COMMENT '奖品价值',
    `coupon_id` BIGINT UNSIGNED NULL COMMENT '关联优惠券ID',
    `total_stock` INT DEFAULT 0 COMMENT '总库存',
    `remain_stock` INT DEFAULT 0 COMMENT '剩余库存',
    `probability` DECIMAL(5,4) DEFAULT 0 COMMENT '中奖概率',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '奖品图片',
    `status` TINYINT DEFAULT 1 COMMENT '1=启用 0=停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`coupon_id`) REFERENCES `coupons`(`coupon_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖池表';

-- ==================== 抽奖记录表 ====================
CREATE TABLE IF NOT EXISTS `lottery_record` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `prize_id` INT NOT NULL COMMENT '奖品ID',
    `prize_name` VARCHAR(100) NOT NULL COMMENT '奖品名称',
    `prize_type` TINYINT NOT NULL COMMENT '奖品类型',
    `prize_value` DECIMAL(10,2) DEFAULT 0 COMMENT '奖品价值',
    `coupon_id` BIGINT UNSIGNED NULL COMMENT '关联优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '0=未发放 1=已发放',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抽奖时间',
    INDEX `idx_lottery_user` (`user_id`),
    INDEX `idx_lottery_prize` (`prize_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖记录表';
