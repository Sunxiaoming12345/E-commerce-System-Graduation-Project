#!/bin/bash
# MySQL 主从复制配置: bysj-mysql (3306) → bysj-mysql-2 (3307)
set -e

MASTER="bysj-mysql"
SLAVE="bysj-mysql-2"
MYSQL="mysql -uroot -p1234"
MASTER_HOST="192.168.100.128"
MASTER_PORT="3306"

echo "=== 1. 创建复制用户 (Master) ==="
docker exec $MASTER $MYSQL -e "
  CREATE USER IF NOT EXISTS 'repl'@'%' IDENTIFIED WITH mysql_native_password BY 'repl123456';
  GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';
  FLUSH PRIVILEGES;
"

echo "=== 2. 获取 Master binlog 位置 ==="
MASTER_STATUS=$(docker exec $MASTER $MYSQL -e "FLUSH TABLES WITH READ LOCK; SHOW MASTER STATUS\G" 2>/dev/null)
echo "$MASTER_STATUS"

LOG_FILE=$(echo "$MASTER_STATUS" | grep 'File:' | awk '{print $2}')
LOG_POS=$(echo "$MASTER_STATUS" | grep 'Position:' | awk '{print $2}')
echo "Master: $LOG_FILE @ $LOG_POS"

echo "=== 3. 导出 Master 数据 ==="
docker exec $MASTER mysqldump -uroot -p1234 --all-databases --master-data=2 --single-transaction > /tmp/master_dump.sql 2>/dev/null
echo "Dump size: $(wc -c < /tmp/master_dump.sql) bytes"

# 释放 Master 锁
docker exec $MASTER $MYSQL -e "UNLOCK TABLES" 2>/dev/null

echo "=== 4. 导入 Slave ==="
docker exec -i $SLAVE $MYSQL < /tmp/master_dump.sql 2>/dev/null
echo "Import done"

echo "=== 5. 配置 Slave 复制 ==="
docker exec $SLAVE $MYSQL -e "
  STOP SLAVE;
  CHANGE MASTER TO
    MASTER_HOST='$MASTER_HOST',
    MASTER_PORT=$MASTER_PORT,
    MASTER_USER='repl',
    MASTER_PASSWORD='repl123456',
    MASTER_LOG_FILE='$LOG_FILE',
    MASTER_LOG_POS=$LOG_POS;
  START SLAVE;
"

echo "=== 6. 验证 Slave 状态 ==="
sleep 2
docker exec $SLAVE $MYSQL -e "SHOW SLAVE STATUS\G" 2>/dev/null | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master|Last_Error"

echo "=== 7. 验证数据同步 ==="
echo "Master cart count:"
docker exec $MASTER $MYSQL -e "SELECT COUNT(*) FROM bysj.cart" 2>/dev/null
echo "Slave cart count:"
docker exec $SLAVE $MYSQL -e "SELECT COUNT(*) FROM bysj.cart" 2>/dev/null

echo "=== 复制配置完成 ==="
