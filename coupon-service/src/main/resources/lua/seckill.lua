-- 秒杀扣库存 Lua 脚本（原子操作）
-- KEYS[1] = seckill:stock:{seckillId}  库存 key
-- KEYS[2] = seckill:bought:{seckillId}:{userId}  防重 key
-- ARGV[1] = stock 扣减数量（默认 1）

local stock = redis.call('get', KEYS[1])
if not stock then
    return -1  -- 商品不存在（缓存未预热）
end
if tonumber(stock) <= 0 then
    return 0   -- 库存不足
end

-- 检查是否已购买
local bought = redis.call('exists', KEYS[2])
if bought == 1 then
    return -2  -- 已购买，不能重复抢
end

-- 扣库存
local after = redis.call('decr', KEYS[1])
if after < 0 then
    redis.call('incr', KEYS[1])  -- 回滚
    return 0
end

-- 标记已购买（1 小时过期）
redis.call('setex', KEYS[2], 3600, '1')
return 1  -- 成功
