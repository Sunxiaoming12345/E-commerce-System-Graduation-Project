package com.example.mailcoupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.constant.LotteryConstant;
import com.example.entity.Coupon;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.constant.LotteryConstant;
import com.example.constant.RabbitMQConstant;
import com.example.entity.Coupon;
import com.example.entity.LotteryPool;
import com.example.entity.LotteryRecord;
import com.example.entity.UserCoupon;
import com.example.exception.BusinessException;
import com.example.mailcoupon.mapper.CouponMapper;
import com.example.mailcoupon.mapper.LotteryMapper;
import com.example.mailcoupon.service.LotteryService;
import com.example.mailcoupon.vo.LotteryPrizeVO;
import com.example.mailcoupon.vo.LotteryRecordVO;
import com.example.mailcoupon.vo.LotterySpinResultVO;
import com.example.result.PageResult;
import com.example.utils.RedisLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LotteryServiceImpl implements LotteryService {

    @Autowired
    private LotteryMapper lotteryMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String RECORDS_CACHE_KEY_PREFIX = "lottery:records:";
    private static final String RECORDS_COUNT_KEY_PREFIX = "lottery:records:count:";
    private static final long RECORDS_CACHE_TTL = 2; // 分钟

    private static final String CONFIG_SPIN_COST_KEY = "lottery:config:spinCost";
    private static final String CONFIG_DAILY_LIMIT_KEY = "lottery:config:dailyLimit";
    private static final String PRIZES_CACHE_KEY = "lottery:prizes:active";
    private static final String TODAY_SPINS_PREFIX = "lottery:spins:";
    private static final long CONFIG_CACHE_TTL = 5; // 分钟

    @Override
    public BigDecimal getSpinCost() {
        String cached = stringRedisTemplate.opsForValue().get(CONFIG_SPIN_COST_KEY);
        if (cached != null) {
            return new BigDecimal(cached);
        }
        BigDecimal cost = lotteryMapper.selectSpinCost();
        if (cost != null) {
            stringRedisTemplate.opsForValue().set(CONFIG_SPIN_COST_KEY, cost.toPlainString(), CONFIG_CACHE_TTL, java.util.concurrent.TimeUnit.MINUTES);
        }
        return cost;
    }

    @Override
    public Integer getDailyLimit() {
        String cached = stringRedisTemplate.opsForValue().get(CONFIG_DAILY_LIMIT_KEY);
        if (cached != null) {
            return Integer.valueOf(cached);
        }
        Integer limit = lotteryMapper.selectDailyLimit();
        limit = limit != null ? limit : 1;
        stringRedisTemplate.opsForValue().set(CONFIG_DAILY_LIMIT_KEY, String.valueOf(limit), CONFIG_CACHE_TTL, java.util.concurrent.TimeUnit.MINUTES);
        return limit;
    }

    @Override
    public int countTodaySpins(Integer userId) {
        String key = TODAY_SPINS_PREFIX + userId + ":" + LocalDate.now();
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Integer.parseInt(cached);
        }
        int count = lotteryMapper.countTodaySpins(userId);
        // 缓存到当天结束，之后自动过期
        stringRedisTemplate.opsForValue().set(key, String.valueOf(count),
                getSecondsUntilMidnight(), java.util.concurrent.TimeUnit.SECONDS);
        return count;
    }

    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, midnight).getSeconds();
    }

    @Override
    public List<LotteryPrizeVO> getActivePrizes() {
        String cached = stringRedisTemplate.opsForValue().get(PRIZES_CACHE_KEY);
        if (cached != null) {
            return JSON.parseArray(cached, LotteryPrizeVO.class);
        }
        List<LotteryPrizeVO> list = lotteryMapper.selectActivePrizes().stream()
                .map(p -> {
                    LotteryPrizeVO vo = new LotteryPrizeVO();
                    vo.setId(p.getId());
                    vo.setPrizeType(p.getPrizeType());
                    vo.setPrizeName(p.getPrizeName());
                    vo.setPrizeImage(p.getPrizeImage());
                    vo.setProbability(p.getProbability());
                    vo.setBalanceAmount(p.getBalanceAmount());
                    vo.setTotalStock(p.getTotalStock());
                    vo.setRemainingStock(p.getRemainingStock());
                    return vo;
                })
                .collect(Collectors.toList());
        stringRedisTemplate.opsForValue().set(PRIZES_CACHE_KEY, JSON.toJSONString(list), CONFIG_CACHE_TTL, java.util.concurrent.TimeUnit.MINUTES);
        return list;
    }

    @Override
    public boolean canSpinToday(Integer userId) {
        Integer limit = getDailyLimit();
        if (limit == null || limit == 0) return true; // 0 = 不限制
        return countTodaySpins(userId) < limit;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotterySpinResultVO spin(Integer userId) {
        // 1. Redis 每日分布式锁
        String lockKey = LotteryConstant.REDIS_DAILY_LOCK_PREFIX + userId + ":" + LocalDate.now();
        String lockToken = redisLockUtil.acquire(lockKey, 10);
        if (lockToken == null) {
            throw new BusinessException("请勿重复操作");
        }

        try {
            // 2. 检查今日次数
            if (!canSpinToday(userId)) {
                throw new BusinessException("今天已经抽过奖了，明天再来吧");
            }

            BigDecimal cost = getSpinCost();

            // 3. 先获取奖池，过滤掉库存为 0 的实物奖品，再抽取
            List<LotteryPool> pools = lotteryMapper.selectActivePrizes().stream()
                    .filter(p -> !LotteryConstant.PRIZE_PHYSICAL.equals(p.getPrizeType())
                            || (p.getRemainingStock() != null && p.getRemainingStock() > 0))
                    .collect(Collectors.toList());

            if (pools.isEmpty()) {
                throw new BusinessException("奖池暂无可抽奖品");
            }

            LotteryPool winner = drawPrize(pools);

            // 4. 如果是实物奖品，先原子扣库存（成功再继续）
            if (LotteryConstant.PRIZE_PHYSICAL.equals(winner.getPrizeType())) {
                int rows = lotteryMapper.decrementStock(winner.getId());
                if (rows == 0) {
                    throw new BusinessException("奖品刚被抢光，请再试一次");
                }
            }

            // 5. 确认能抽到奖品后，再扣余额
            BigDecimal newBalance;
            try {
                ResponseEntity<String> resp = restTemplate.postForEntity(
                        "http://user-service/user/balance/internal/decrease?userId=" + userId + "&amount=" + cost,
                        null, String.class);
                if (resp.getBody() != null) {
                    JSONObject json = JSON.parseObject(resp.getBody());
                    if (json.getInteger("code") == null || json.getInteger("code") != 1) {
                        String msg = json.getString("msg");
                        throw new BusinessException(msg != null ? msg : "余额不足");
                    }
                    newBalance = json.get("data") != null ? json.getBigDecimal("data") : null;
                } else {
                    throw new BusinessException("服务异常，请稍后重试");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("扣减余额失败: userId={}, cost={}", userId, cost, e);
                throw new BusinessException("服务异常，请稍后重试");
            }

            // 6. 构建记录并发放奖品
            LotteryRecord record = new LotteryRecord();
            record.setUserId(userId);
            record.setSpinCost(cost);

            LotterySpinResultVO.LotterySpinResultVOBuilder resultBuilder = LotterySpinResultVO.builder()
                    .spinCost(cost)
                    .newBalance(newBalance);

            if (winner == null || LotteryConstant.PRIZE_THANKS.equals(winner.getPrizeType())) {
                record.setPrizeType(LotteryConstant.PRIZE_THANKS);
                record.setPrizeName("谢谢参与");
                record.setPrizeId(winner != null ? winner.getId() : null);
                resultBuilder.prizeType(LotteryConstant.PRIZE_THANKS)
                        .prizeName("谢谢参与")
                        .prizeId(winner != null ? winner.getId() : null);

            } else if (LotteryConstant.PRIZE_COUPON.equals(winner.getPrizeType())) {
                Coupon coupon = couponMapper.selectById(winner.getCouponId());
                if (coupon == null) {
                    throw new BusinessException("奖品优惠券已失效");
                }
                UserCoupon uc = new UserCoupon();
                uc.setUserId(userId);
                uc.setCouponId(winner.getCouponId());
                couponMapper.insertUserCoupon(uc);
                couponMapper.incrementUsedCount(winner.getCouponId());

                record.setPrizeType(LotteryConstant.PRIZE_COUPON);
                record.setPrizeName(winner.getPrizeName());
                record.setPrizeId(winner.getId());
                record.setCouponId(winner.getCouponId());
                resultBuilder.prizeType(LotteryConstant.PRIZE_COUPON)
                        .prizeName(winner.getPrizeName())
                        .prizeId(winner.getId())
                        .prizeImage(winner.getPrizeImage())
                        .couponName(coupon.getName());

            } else if (LotteryConstant.PRIZE_BALANCE.equals(winner.getPrizeType())) {
                try {
                    ResponseEntity<String> resp = restTemplate.postForEntity(
                            "http://user-service/user/balance/internal/increase?userId=" + userId + "&amount=" + winner.getBalanceAmount(),
                            null, String.class);
                    if (resp.getBody() != null) {
                        JSONObject json = JSON.parseObject(resp.getBody());
                        if (json.getInteger("code") != null && json.getInteger("code") == 1
                                && json.get("data") != null) {
                            newBalance = json.getBigDecimal("data");
                            resultBuilder.newBalance(newBalance);
                        }
                    }
                } catch (BusinessException e) {
                    throw e;
                } catch (Exception e) {
                    log.error("发放余额失败: userId={}, amount={}", userId, winner.getBalanceAmount(), e);
                    throw new BusinessException("余额发放失败，请联系客服");
                }
                record.setPrizeType(LotteryConstant.PRIZE_BALANCE);
                record.setPrizeName(winner.getPrizeName());
                record.setPrizeId(winner.getId());
                record.setBalanceAmount(winner.getBalanceAmount());
                resultBuilder.prizeType(LotteryConstant.PRIZE_BALANCE)
                        .prizeName(winner.getPrizeName())
                        .prizeId(winner.getId())
                        .prizeImage(winner.getPrizeImage())
                        .balanceAmount(winner.getBalanceAmount());

            } else if (LotteryConstant.PRIZE_PHYSICAL.equals(winner.getPrizeType())) {
                // 库存已在第4步原子扣减，这里只需记录
                record.setPrizeType(LotteryConstant.PRIZE_PHYSICAL);
                record.setPrizeName(winner.getPrizeName());
                record.setPrizeId(winner.getId());
                record.setFulfillmentStatus(LotteryConstant.FULFILLMENT_PENDING);
                resultBuilder.prizeType(LotteryConstant.PRIZE_PHYSICAL)
                        .prizeName(winner.getPrizeName())
                        .prizeId(winner.getId())
                        .prizeImage(winner.getPrizeImage());
            }

            lotteryMapper.insertRecord(record);
            // 更新 Redis 今日抽奖次数
            String todayKey = TODAY_SPINS_PREFIX + userId + ":" + LocalDate.now();
            stringRedisTemplate.opsForValue().increment(todayKey);
            stringRedisTemplate.expire(todayKey, getSecondsUntilMidnight(), java.util.concurrent.TimeUnit.SECONDS);
            log.info("用户 {} 抽奖获得: {} (扣减 ¥{})", userId, record.getPrizeName(), cost);
            // 通过 RabbitMQ 异步刷新缓存
            JSONObject cacheMsg = new JSONObject();
            cacheMsg.put("userId", userId);
            rabbitTemplate.convertAndSend(RabbitMQConstant.LOTTERY_EXCHANGE_NAME, RabbitMQConstant.LOTTERY_ROUTING_KEY, cacheMsg.toJSONString());
            return resultBuilder.build();

        } finally {
            redisLockUtil.release(lockKey, lockToken);
        }
    }

    /**
     * 加权随机抽奖算法 — 按概率累积分布抽取
     */
    private LotteryPool drawPrize(List<LotteryPool> pools) {
        if (pools == null || pools.isEmpty()) return null;

        BigDecimal total = pools.stream()
                .map(LotteryPool::getProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) return null;

        BigDecimal rand = total.multiply(BigDecimal.valueOf(Math.random()));
        BigDecimal cumulative = BigDecimal.ZERO;

        for (LotteryPool p : pools) {
            cumulative = cumulative.add(p.getProbability());
            if (rand.compareTo(cumulative) < 0) {
                return p;
            }
        }
        return pools.get(pools.size() - 1);
    }

    @Override
    public PageResult getMyRecords(Integer userId, int page, int pageSize) {
        String pageKey = RECORDS_CACHE_KEY_PREFIX + userId + ":" + page + ":" + pageSize;
        String countKey = RECORDS_COUNT_KEY_PREFIX + userId;
        String cachedPage = stringRedisTemplate.opsForValue().get(pageKey);
        String cachedCount = stringRedisTemplate.opsForValue().get(countKey);
        if (cachedPage != null && cachedCount != null) {
            List<LotteryRecordVO> vos = JSON.parseArray(cachedPage, LotteryRecordVO.class);
            return new PageResult(Long.parseLong(cachedCount), vos);
        }

        int total = lotteryMapper.countRecordsByUserId(userId);
        int offset = (page - 1) * pageSize;
        List<LotteryRecord> records = lotteryMapper.selectRecordsByUserIdPage(userId, offset, pageSize);

        List<LotteryRecordVO> vos = records.stream().map(r -> {
            LotteryRecordVO vo = new LotteryRecordVO();
            vo.setId(r.getId());
            vo.setPrizeType(r.getPrizeType());
            vo.setPrizeName(r.getPrizeName());
            vo.setBalanceAmount(r.getBalanceAmount());
            vo.setSpinCost(r.getSpinCost());
            vo.setFulfillmentStatus(r.getFulfillmentStatus());
            vo.setShippingInfo(r.getShippingInfo());
            vo.setCreateTime(r.getCreateTime());
            if (r.getPrizeId() != null) {
                LotteryPool pool = lotteryMapper.selectPrizeById(r.getPrizeId());
                if (pool != null) vo.setPrizeImage(pool.getPrizeImage());
            }
            if (r.getCouponId() != null) {
                Coupon coupon = couponMapper.selectById(r.getCouponId());
                if (coupon != null) vo.setCouponName(coupon.getName());
            }
            return vo;
        }).collect(Collectors.toList());

        // 分开缓存 List 和 total，避免泛型擦除导致反序列化失败
        stringRedisTemplate.opsForValue().set(pageKey, JSON.toJSONString(vos), RECORDS_CACHE_TTL, java.util.concurrent.TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(countKey, String.valueOf(total), RECORDS_CACHE_TTL, java.util.concurrent.TimeUnit.MINUTES);
        return new PageResult((long) total, vos);
    }

}
