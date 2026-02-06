package com.example.gateway.mapper;

import com.example.gateway.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface AdminMapper {

    @Select("select * from admin where username=#{username} and password=#{password}")
    Admin adminLogin(@Param("username") String username, @Param("password") String password);
}