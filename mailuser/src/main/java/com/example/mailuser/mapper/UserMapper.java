package com.example.mailuser.mapper;

import com.example.mailuser.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from user where id=#{userId}")
    User getUserById(@Param("userId") Long userId);

    void updateUser(User user);
}