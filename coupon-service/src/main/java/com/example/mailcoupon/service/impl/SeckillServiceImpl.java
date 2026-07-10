package com.example.mailcoupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.constant.RabbitMQConstant;
import com.example.constant.SeckillConstant;
import com.example.entity.SeckillMessageDTO;
import com.example.mailcoupon.entity.SeckillProduct;
import com.example.mailcoupon.mapper.SeckillProductMapper;
import com.example.mailcoupon.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillProductMapper seckillProductMapper;
    private final RabbitTemplate rabbitTemplate;
    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setLocation(new ClassPathResource("lua/seckill.lua"));
        seckillScript.setResultType(Long.class);
    }

    @Override
    public void warmUp(Long seckillId) {
        SeckillProduct product = seckillProductMapper.selectById(seckillId);
        if (product == null) return;
        stringRedisTemplate.opsForValue().set(
                SeckillConstant.STOCK_KEY_PREFIX + seckillId,
                String.valueOf(product.getStock()),
                Duration.ofHours(2));
        String json = JSON.toJSONString(product);
        stringRedisTemplate.opsForValue().set(
                SeckillConstant.PRODUCT_KEY_PREFIX + seckillId,
                json,
                Duration.ofHours(2));
        log.info("秒杀预热完成: seckillId={}, stock={}", seckillId, product.getStock());
    }

    // ===================== 管理员 CRUD =====================

    @Override
    public List<SeckillProduct> listAll() {
        List<SeckillProduct> list = seckillProductMapper.selectAll();
        // 根据时间自动修正状态
        LocalDateTime now = LocalDateTime.now();
        for (SeckillProduct p : list) {
            if (p.getStatus() == SeckillConstant.STATUS_ENDED) continue;
            if (now.isBefore(p.getStartTime())) p.setStatus(SeckillConstant.STATUS_UPCOMING);
            else if (now.isAfter(p.getEndTime())) p.setStatus(SeckillConstant.STATUS_ENDED);
            else p.setStatus(SeckillConstant.STATUS_ACTIVE);
        }
        return list;
    }

    @Override
    public SeckillProduct getById(Long id) { return seckillProductMapper.selectById(id); }

    @Override
    public SeckillProduct create(SeckillProduct product) {
        product.setStatus(SeckillConstant.STATUS_UPCOMING);
        product.setOriginStock(product.getStock());
        seckillProductMapper.insert(product);
        warmUp(product.getId());
        return product;
    }

    @Override
    public SeckillProduct update(Long id, SeckillProduct product) {
        product.setId(id);
        if (product.getStock() != null) product.setOriginStock(product.getStock());
        seckillProductMapper.updateById(product);
        warmUp(id);
        return seckillProductMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        seckillProductMapper.deleteById(id);
        stringRedisTemplate.delete(SeckillConstant.STOCK_KEY_PREFIX + id);
        stringRedisTemplate.delete(SeckillConstant.PRODUCT_KEY_PREFIX + id);
    }

    // ===================== 用户端 =====================

    @Override
    public List<SeckillProduct> getActiveList() {
        List<SeckillProduct> list = seckillProductMapper.selectActiveList();
        LocalDateTime now = LocalDateTime.now();
        for (SeckillProduct p : list) {
            if (now.isBefore(p.getStartTime())) p.setStatus(SeckillConstant.STATUS_UPCOMING);
            else if (now.isAfter(p.getEndTime())) p.setStatus(SeckillConstant.STATUS_ENDED);
            else p.setStatus(SeckillConstant.STATUS_ACTIVE);
            // 实时库存从 Redis 读
            String stockStr = stringRedisTemplate.opsForValue()
                    .get(SeckillConstant.STOCK_KEY_PREFIX + p.getId());
            if (stockStr != null) p.setStock(Integer.parseInt(stockStr));
        }
        return list;
    }

    @Override
    public SeckillProduct getDetail(Long seckillId) {
        SeckillProduct p = seckillProductMapper.selectById(seckillId);
        if (p == null) return null;
        String stockStr = stringRedisTemplate.opsForValue()
                .get(SeckillConstant.STOCK_KEY_PREFIX + seckillId);
        if (stockStr != null) p.setStock(Integer.parseInt(stockStr));
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(p.getStartTime())) p.setStatus(SeckillConstant.STATUS_UPCOMING);
        else if (now.isAfter(p.getEndTime())) p.setStatus(SeckillConstant.STATUS_ENDED);
        else p.setStatus(SeckillConstant.STATUS_ACTIVE);
        return p;
    }

    @Override
    public int executeSeckill(Long seckillId, Integer userId) {
        // 1. Redis Lua 原子扣库存
        String stockKey = SeckillConstant.STOCK_KEY_PREFIX + seckillId;
        String boughtKey = SeckillConstant.BOUGHT_KEY_PREFIX + seckillId + ":" + userId;
        Long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(stockKey, boughtKey)  // KEYS[1]=stock, KEYS[2]=bought
        );

        if (result == null || result <= 0) {
            if (result != null && result == -2) log.warn("用户重复秒杀: userId={}, seckillId={}", userId, seckillId);
            else if (result != null && result == -1) log.warn("秒杀缓存未预热: seckillId={}", seckillId);
            else log.info("库存不足: seckillId={}, userId={}", seckillId, userId);
            return result == null ? 0 : result.intValue();
        }

        // 2. 获取秒杀商品信息
        SeckillProduct product = seckillProductMapper.selectById(seckillId);
        if (product == null) {
            // 回滚 Redis
            stringRedisTemplate.opsForValue().increment(stockKey);
            stringRedisTemplate.delete(boughtKey);
            return 0;
        }

        // 3. 发送 MQ 削峰（异步创建订单）
        SeckillMessageDTO msg = new SeckillMessageDTO();
        msg.setSeckillId(seckillId);
        msg.setProductId(product.getProductId());
        msg.setProductName(product.getProductName());
        msg.setUserId(userId);
        msg.setPrice(product.getSeckillPrice());

        rabbitTemplate.convertAndSend(
                RabbitMQConstant.SECKILL_EXCHANGE_NAME,
                RabbitMQConstant.SECKILL_ROUTING_KEY,
                msg);

        log.info("秒杀成功: seckillId={}, userId={}, price={}", seckillId, userId, product.getSeckillPrice());
        return 1;
    }
}
