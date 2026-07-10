package com.example.gateway.mapper;

import com.example.gateway.entity.LoginLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper {

    @Insert("INSERT INTO login_logs (username, ip, login_time) VALUES (#{username}, #{ip}, #{loginTime})")
    void insert(LoginLog loginLog);
}
