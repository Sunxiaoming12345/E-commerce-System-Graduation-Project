package com.example.gateway.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginInfoDTO implements Serializable {
    private String username;
    private String password;
}
