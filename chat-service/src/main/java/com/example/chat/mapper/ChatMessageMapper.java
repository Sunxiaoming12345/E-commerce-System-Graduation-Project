package com.example.chat.mapper;

import com.example.entity.ChatMessage;
import com.example.chat.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Insert("INSERT INTO chat_messages (sender_id, sender_type, product_id, content, is_read, create_time) " +
            "VALUES (#{senderId}, #{senderType}, #{productId}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    List<ChatMessage> selectByProductAndUser(@Param("productId") Long productId,
                                             @Param("userId") Integer userId,
                                             @Param("adminId") Integer adminId);

    @Update("UPDATE chat_messages SET is_read = 1 WHERE sender_type = 0 AND product_id = #{productId} AND sender_id = #{userId} AND is_read = 0")
    int markRead(@Param("productId") Long productId, @Param("userId") Integer userId);

    List<ChatSessionVO> selectSessions(@Param("adminId") Integer adminId);
}
