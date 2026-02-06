package com.example.gateway.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminLoginInfoDTO implements Serializable {
    private String username;
    private String password;
}
