package com.example.mailuser.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private String username;
    private String password;
    private String phone;
    private String defaultReceiver;    // 常用收货人
    private String defaultPhone;       // 常用联系电话
    private String defaultAddress;     // 常用收货地址
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
