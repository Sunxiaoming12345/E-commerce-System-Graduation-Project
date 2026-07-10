package com.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer senderId;
    private Integer senderType;   // 0=用户 1=管理员
    private Long productId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}
