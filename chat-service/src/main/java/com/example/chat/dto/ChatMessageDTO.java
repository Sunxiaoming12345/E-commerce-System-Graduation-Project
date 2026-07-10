package com.example.chat.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long productId;
    private String content;
    private Integer receiverId;  // 管理员回复时指定目标用户ID
}
