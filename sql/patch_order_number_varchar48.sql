-- 已有库若仍为 VARCHAR(32)，请执行以容纳「时间戳+用户ID+4位字母数字」订单号
ALTER TABLE `orders` MODIFY COLUMN `order_number` VARCHAR(48) NOT NULL COMMENT '订单编号';
