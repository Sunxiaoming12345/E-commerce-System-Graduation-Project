package com.example.gateway.mapper;

import com.example.gateway.entity.Balance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;

/**
 * 余额Mapper
 *
 * @author sunxiaoming
 * @date 2026-03-11
 */
@Mapper
public interface BalanceMapper {

    /**
     * 创建余额记录
     *
     * @param balance 余额信息
     * @return 影响行数
     */
    @Insert("insert into balance(user_id, balance, create_time, update_time) values(#{userId}, #{balance}, #{createTime}, #{updateTime})")
    int createBalance(Balance balance);

}
