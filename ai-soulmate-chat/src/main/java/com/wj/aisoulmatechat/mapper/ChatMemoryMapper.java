package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.ChatMemoryEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatMemoryMapper extends BaseMapper<ChatMemoryEntity> {
    /**
     * 批量增量插入，IGNORE自动跳过重复msg_uuid，幂等防重复消费
     */
    @Insert("<script>" +
            "INSERT IGNORE INTO ai_soulmate_chat_memory " +
            "(msg_uuid,conversation_id,msg_type,content,metadata,media,tool_calls,tool_responses) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.msgUuid},#{item.conversationId},#{item.msgType},#{item.content},#{item.metadata},#{item.media},#{item.toolCalls},#{item.toolResponses})" +
            "</foreach>" +
            "</script>")
    int batchInsertIgnore(@Param("list") List<ChatMemoryEntity> entityList);

    @Select("SELECT t.* FROM ai_soulmate_chat_memory t WHERE conversation_id = #{conversationId} ORDER BY create_time ASC")
    List<ChatMemoryEntity> listAllByConversation(@Param("conversationId") String conversationId);


    /**
     * 根据soulmateId删除该伴侣全部聊天记录
     * @param convId 会话ID
     * @return 删除影响行数
     */
    @Delete("delete from ai_soulmate_chat_memory where conversation_id = #{convId}")
    int deleteByConversationId(@Param("convId") String convId);

}
