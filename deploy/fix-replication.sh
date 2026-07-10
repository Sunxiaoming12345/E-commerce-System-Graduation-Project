#!/bin/bash
# 修复步骤：在已有数据导入的基础上配置 Slave 复制（MySQL 8.0 语法）
SLAVE="bysj-mysql-2"
MYSQL="mysql -uroot -p1234"

echo "=== 配置 Slave 复制 (MySQL 8.0 CHANGE REPLICATION SOURCE TO) ==="
docker exec $SLAVE $MYSQL <<SQL
  STOP SLAVE;
  CHANGE REPLICATION SOURCE TO
    SOURCE_HOST='192.168.100.128',
    SOURCE_PORT=3306,
    SOURCE_USER='repl',
    SOURCE_PASSWORD='repl123456',
    SOURCE_LOG_FILE='binlog.000040',
    SOURCE_LOG_POS=3495;
  START REPLICA;
SQL

echo "=== 等待复制启动 ==="
sleep 2

echo "=== Slave 复制状态 ==="
docker exec $SLAVE $MYSQL -e "SHOW REPLICA STATUS\G" 2>/dev/null | grep -E "Replica_IO_Running|Replica_SQL_Running|Seconds_Behind_Source|Last_Error|Last_IO_Error"

echo ""
echo "=== 数据验证 ==="
echo "Master cart count:"
docker exec bysj-mysql $MYSQL -e "SELECT COUNT(*) AS cnt FROM bysj.cart" 2>/dev/null
echo "Slave cart count:"
docker exec $SLAVE $MYSQL -e "SELECT COUNT(*) AS cnt FROM bysj.cart" 2>/dev/null
