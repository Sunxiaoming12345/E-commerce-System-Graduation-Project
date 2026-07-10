package com.example.userservice.mapper;

import com.example.userservice.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where id=#{userId}")
    User getUserById(@Param("userId") Long userId);

    void updateUser(User user);

    @org.apache.ibatis.annotations.Update("update user set password=#{password}, update_time=now() where id=#{id}")
    int updatePassword(@org.apache.ibatis.annotations.Param("id") Long id, @org.apache.ibatis.annotations.Param("password") String password);
}