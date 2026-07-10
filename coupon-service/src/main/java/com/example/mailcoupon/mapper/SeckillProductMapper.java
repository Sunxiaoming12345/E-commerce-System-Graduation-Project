package com.example.mailcoupon.mapper;

import com.example.mailcoupon.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeckillProductMapper {

    List<SeckillProduct> selectActiveList();

    SeckillProduct selectById(@Param("id") Long id);

    int insert(SeckillProduct product);

    int updateById(SeckillProduct product);

    int deleteById(@Param("id") Long id);

    /** 扣减库存（乐观锁），返回影响行数 */
    int deductStock(@Param("id") Long id, @Param("version") Integer version);

    List<SeckillProduct> selectAll();
}
