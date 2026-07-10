package com.example.chat.service;

import com.example.chat.vo.ChatSessionVO;
import com.example.entity.ChatMessage;
import java.util.List;

public interface ChatMessageService {
    ChatMessage save(Integer senderId, Integer senderType, Long productId, String content);
    List<ChatMessage> getHistory(Long productId, Integer userId, Integer adminId);
    List<ChatSessionVO> getSessions(Integer adminId);
    void markRead(Long productId, Integer userId);
}
