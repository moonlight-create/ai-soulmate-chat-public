package com.wj.aisoulmatechat.config.memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import org.springframework.ai.content.Media;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomChatMemoryDTO {
    // 0:SYSTEM 1:USER 2:ASSISTANT 3:TOOL
    private Integer type;
    // 消息文本
    private String content;
    // 通用元数据
    private Map<String, Object> metadata = Map.of();
    // UserMessage多媒体图片
    private List<Media> media = new ArrayList<>();
    // Assistant 函数调用
    private List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
    // Tool工具返回消息
    private List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
    // 预留扩展字段
    private Map<String, Object> properties = Map.of();
}
