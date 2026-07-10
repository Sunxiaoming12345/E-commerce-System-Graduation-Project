package com.example.chat.service.impl;

import com.example.chat.mapper.ChatMessageMapper;
import com.example.chat.service.ChatMessageService;
import com.example.chat.vo.ChatSessionVO;
import com.example.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper mapper;

    @Override
    public ChatMessage save(Integer senderId, Integer senderType, Long productId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(senderId);
        msg.setSenderType(senderType);
        msg.setProductId(productId);
        msg.setContent(content);
        mapper.insert(msg);
        return msg;
    }

    @Override
    public List<ChatMessage> getHistory(Long productId, Integer userId, Integer adminId) {
        return mapper.selectByProductAndUser(productId, userId, adminId);
    }

    @Override
    public List<ChatSessionVO> getSessions(Integer adminId) {
        return mapper.selectSessions(adminId);
    }

    @Override
    public void markRead(Long productId, Integer userId) {
        mapper.markRead(productId, userId);
    }
}
