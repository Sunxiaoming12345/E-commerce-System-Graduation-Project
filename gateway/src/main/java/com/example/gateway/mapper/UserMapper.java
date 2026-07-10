package com.example.gateway.mapper;

import com.example.gateway.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username} and password=#{password}")
    User login(@Param("username") String username, @Param("password") String password);

    @Select("select * from user where username=#{username}")
    User findByUsername(@Param("username") String username);

    @Select("select * from user where phone=#{phone}")
    User findByPhone(@Param("phone") String phone);

    @Select("select * from user where id=#{id}")
    User findById(@Param("id") Long id);

    @Insert("insert into user(name, username, password, phone, create_time, update_time) values(#{name}, #{username}, #{password}, #{phone}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("update user set password=#{password}, update_time=now() where id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Update("update user set name=#{name}, phone=#{phone}, default_receiver=#{defaultReceiver}, default_phone=#{defaultPhone}, default_address=#{defaultAddress}, avatar=COALESCE(#{avatar}, avatar), update_time=now() where id=#{id}")
    int updateProfile(User user);

    @Update("update user set avatar=#{avatar}, update_time=now() where id=#{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);
}
