#!/bin/bash
set -e
# Fix nginx: remove dir if exists, create file, copy config
rm -rf /root/nginx/nginx.conf
cp /root/nginx/nginx-lottery.conf /root/nginx/nginx.conf 2>/dev/null || true
ls -la /root/nginx/nginx.conf

# Restart nginx container
docker rm -f frontend-nginx 2>/dev/null || true
docker run -d --name frontend-nginx --restart unless-stopped \
  --network microservice-net \
  -p 80:80 -p 81:81 \
  -v /root/nginx/html/user:/usr/share/nginx/html/user \
  -v /root/nginx/html/admin:/usr/share/nginx/html/admin \
  -v /root/nginx/nginx.conf:/etc/nginx/nginx.conf:ro \
  nginx:1.20.2

sleep 2
echo "=== testing ==="
curl -s -o /dev/null -w "%{http_code}" http://localhost/user/lottery/prizes
echo " /user/lottery"
curl -s -o /dev/null -w "%{http_code}" http://localhost/admin/lottery/config
echo " /admin/lottery"
