-- 插入管理员数据
INSERT INTO `admin` (`name`, `username`, `password`, `phone`) VALUES
('系统管理员', 'admin', '123456', '13800138000'),
('运营管理员', 'operator', '123456', '13800138001'),
('客服管理员', 'service', '123456', '13800138002');

-- 插入分类数据
INSERT INTO `categories` (`name`, `description`, `sort`) VALUES
('电子产品', '手机、电脑、平板等电子设备', 1),
('服装鞋帽', '衣服、鞋子、帽子等穿戴用品', 2),
('家居用品', '家具、厨具、装饰品等家居产品', 3),
('食品饮料', '零食、饮料、水果等食品', 4),
('美妆个护', '化妆品、护肤品、个人护理用品', 5);

-- 插入商品数据
INSERT INTO `products` (`name`, `description`, `image_url`, `category_id`, `price`, `stock`, `status`) VALUES
('iPhone 15 Pro', '苹果最新款智能手机，搭载A17 Pro芯片', 'https://example.com/iphone15pro.jpg', 1, 8999.00, 50, 1),
('MacBook Pro 16', '苹果高端笔记本电脑，M3 Max芯片', 'https://example.com/macbookpro16.jpg', 1, 19999.00, 20, 1),
('AirPods Pro 2', '苹果降噪耳机，支持空间音频', 'https://example.com/airpodspro2.jpg', 1, 1899.00, 100, 1),
('Nike Air Max', '耐克运动鞋，舒适透气', 'https://example.com/nikeairmax.jpg', 2, 899.00, 80, 1),
('Adidas Originals', '阿迪达斯经典款运动鞋', 'https://example.com/adidasoriginals.jpg', 2, 799.00, 60, 1),
('宜家书桌', '简约现代风格书桌，环保材质', 'https://example.com/ikeadesk.jpg', 3, 1299.00, 30, 1),
('小米空气净化器', '智能空气净化器，除霾除菌', 'https://example.com/xiaomipurifier.jpg', 3, 899.00, 40, 1),
('三只松鼠坚果礼盒', '混合坚果礼盒，送礼佳品', 'https://example.com/squirrelnuts.jpg', 4, 199.00, 200, 1),
('星巴克咖啡豆', '精选阿拉比卡咖啡豆，新鲜烘焙', 'https://example.com/starbuckscoffee.jpg', 4, 89.00, 150, 1),
('SK-II神仙水', '日本知名护肤品，改善肌肤质地', 'https://example.com/sk2.jpg', 5, 1450.00, 60, 1);

-- 插入订单数据
INSERT INTO `orders` (`user_id`, `order_number`, `order_status`, `payment_method`, `total_amount`, `shipping_address`, `receiver_name`, `receiver_phone`, `pay_time`, `ship_time`, `complete_time`) VALUES
(1, 'ORD-20260201-001', 4, 0, 9198.00, '北京市朝阳区某某街道123号', '张三', '13800138001', '2026-02-01 10:30:00', '2026-02-01 14:00:00', '2026-02-03 16:00:00'),
(2, 'ORD-20260201-002', 3, 1, 1698.00, '上海市浦东新区某某路456号', '李四', '13800138002', '2026-02-01 11:00:00', '2026-02-01 15:00:00', NULL),
(3, 'ORD-20260201-003', 2, 2, 1899.00, '广州市天河区某某大道789号', '王五', '13800138003', '2026-02-01 12:00:00', NULL, NULL),
(4, 'ORD-20260201-004', 1, 0, 899.00, '深圳市南山区某某路321号', '赵六', '13800138004', '2026-02-01 13:00:00', NULL, NULL),
(5, 'ORD-20260201-005', 0, 1, 199.00, '杭州市西湖区某某路654号', '钱七', '13800138005', NULL, NULL, NULL);

-- 插入订单项数据
INSERT INTO `order_items` (`order_id`, `product_id`, `product_name`, `product_price`, `quantity`, `subtotal`) VALUES
(1, 1, 'iPhone 15 Pro', 8999.00, 1, 8999.00),
(1, 3, 'AirPods Pro 2', 1899.00, 1, 1899.00),
(2, 4, 'Nike Air Max', 899.00, 2, 1798.00),
(3, 6, '宜家书桌', 1299.00, 1, 1299.00),
(3, 7, '小米空气净化器', 899.00, 1, 899.00),
(4, 8, '三只松鼠坚果礼盒', 199.00, 2, 398.00),
(4, 9, '星巴克咖啡豆', 89.00, 6, 534.00),
(5, 10, 'SK-II神仙水', 1450.00, 1, 1450.00);

-- 插入支付记录数据
INSERT INTO `payments` (`order_id`, `payment_method`, `amount`, `status`, `pay_time`) VALUES
(1, 0, 9198.00, 1, '2026-02-01 10:30:00'),
(2, 1, 1698.00, 1, '2026-02-01 11:00:00'),
(3, 2, 1899.00, 1, '2026-02-01 12:00:00'),
(4, 0, 899.00, 1, '2026-02-01 13:00:00'),
(5, 1, 199.00, 0, NULL);
