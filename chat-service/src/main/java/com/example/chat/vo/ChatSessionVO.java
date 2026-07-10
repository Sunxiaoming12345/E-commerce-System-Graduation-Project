package com.example.chat.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private Integer userId;
    private String userName;
    private Long productId;
    private String productName;
    private String lastMessage;
    private Integer unreadCount;
    private LocalDateTime lastTime;
}
