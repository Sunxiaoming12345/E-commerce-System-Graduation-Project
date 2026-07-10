-- 插入测试奖品 (概率之和 ≈ 1)
DELETE FROM lottery_pool;
INSERT INTO lottery_pool (prize_type, prize_name, prize_image, coupon_id, balance_amount, probability, total_stock, remaining_stock, status) VALUES
('THANKS', '谢谢参与', '', NULL, NULL, 0.3000, NULL, NULL, 1),
('COUPON', '满50减10优惠券', '', 1, NULL, 0.2000, NULL, NULL, 1),
('COUPON', '满100减30优惠券', '', 2, NULL, 0.1500, NULL, NULL, 1),
('BALANCE', '5元余额', '', NULL, 5.00, 0.1500, NULL, NULL, 1),
('BALANCE', '10元余额', '', NULL, 10.00, 0.1000, NULL, NULL, 1),
('BALANCE', '50元余额', '', NULL, 50.00, 0.0500, NULL, NULL, 1),
('PHYSICAL', '最新款手机', 'https://img.alicdn.com/example/phone.png', NULL, NULL, 0.0300, 3, 3, 1),
('PHYSICAL', '品牌笔记本电脑', 'https://img.alicdn.com/example/laptop.png', NULL, NULL, 0.0200, 2, 2, 1);
SELECT COUNT(*) AS pool_count FROM lottery_pool;
