DROP TABLE IF EXISTS login_logs;
CREATE TABLE login_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    ip VARCHAR(50) NOT NULL COMMENT '登录IP',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    INDEX idx_username (username),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录成功日志';
