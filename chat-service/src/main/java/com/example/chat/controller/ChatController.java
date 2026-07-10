package com.example.chat.controller;

import com.example.chat.dto.ChatMessageDTO;
import com.example.chat.service.ChatMessageService;
import com.example.chat.vo.ChatSessionVO;
import com.example.entity.ChatMessage;
import com.example.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "客服聊天")
public class ChatController {

    private final ChatMessageService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void handleChat(ChatMessageDTO dto, Principal principal) {
        String[] parts = principal.getName().split(":");
        String role = parts[0];
        Integer senderId = Integer.parseInt(parts[1]);
        int senderType = "admin".equals(role) ? 1 : 0;

        ChatMessage saved = service.save(senderId, senderType, dto.getProductId(), dto.getContent());

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", saved.getId());
        payload.put("senderId", saved.getSenderId());
        payload.put("senderType", saved.getSenderType());
        payload.put("productId", saved.getProductId());
        payload.put("content", saved.getContent());
        payload.put("createTime", java.time.LocalDateTime.now().toString().replace("T", " "));

        if (senderType == 0) {
            // 用户发消息 → 推送给所有管理员
            messagingTemplate.convertAndSend("/topic/admin", payload);
        } else {
            // 管理员回复 → 推送给目标用户
            Integer targetUserId = dto.getReceiverId();
            if (targetUserId != null) {
                messagingTemplate.convertAndSend("/topic/user." + targetUserId, payload);
            }
        }
    }

    @GetMapping("/user/chat/history")
    @ApiOperation("获取聊天历史")
    public Result<List<ChatMessage>> history(@RequestParam Long productId,
                                              @RequestParam Integer userId,
                                              @RequestParam(defaultValue = "1") Integer adminId) {
        return Result.success(service.getHistory(productId, userId, adminId));
    }

    @GetMapping("/admin/chat/sessions")
    @ApiOperation("管理员获取客服会话列表")
    public Result<List<ChatSessionVO>> sessions(@RequestParam(defaultValue = "1") Integer adminId) {
        return Result.success(service.getSessions(adminId));
    }

    @PutMapping("/user/chat/read")
    @ApiOperation("标记消息已读")
    public Result<Void> markRead(@RequestParam Long productId, @RequestParam Integer userId) {
        service.markRead(productId, userId);
        return Result.success();
    }
}
