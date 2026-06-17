package com.wj.aisoulmatechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_soulmate_chat_memory")
public class ChatMemoryEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 单条消息唯一标识
    private String msgUuid;
    // 会话ID
    private String conversationId;
    // 0:SYSTEM 1:USER 2:ASSISTANT 3:TOOL
    private Integer msgType;
    // 消息文本
    private String content;
    // 元数据JSON
    private String metadata;
    // 媒体资源JSON
    private String media;
    // 工具调用JSON
    private String toolCalls;
    // 工具响应JSON
    private String toolResponses;
    // 入库时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
