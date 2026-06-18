package com.wj.aisoulmatechat.entity.mq;

import com.wj.aisoulmatechat.config.memory.CustomChatMemoryDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ChatMemoryIncrementMsg implements Serializable {
    private String conversationId;
    private List<CustomChatMemoryDTO> dtoList;
}
