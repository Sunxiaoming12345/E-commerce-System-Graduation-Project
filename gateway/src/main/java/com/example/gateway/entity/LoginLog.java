package com.example.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLog {
    private Long id;
    private String username;
    private String ip;
    private LocalDateTime loginTime;
}
