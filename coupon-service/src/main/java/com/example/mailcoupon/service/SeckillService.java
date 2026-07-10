package com.example.mailcoupon.service;

import com.example.mailcoupon.entity.SeckillProduct;
import java.util.List;

public interface SeckillService {
    /** 预热秒杀商品到 Redis */
    void warmUp(Long seckillId);

    /** 管理员 CRUD */
    List<SeckillProduct> listAll();
    SeckillProduct getById(Long id);
    SeckillProduct create(SeckillProduct product);
    SeckillProduct update(Long id, SeckillProduct product);
    void delete(Long id);

    /** 用户端：获取进行中的秒杀列表 */
    List<SeckillProduct> getActiveList();

    /** 用户端：获取单个秒杀详情（含实时库存） */
    SeckillProduct getDetail(Long seckillId);

    /** 用户端：执行秒杀（Redis Lua 扣库存 + 发 MQ） */
    int executeSeckill(Long seckillId, Integer userId);
}
