package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.config.memory.CustomChatMemoryDTO;
import com.wj.aisoulmatechat.vo.ChatMemoryGroupVO;

import java.util.List;

public interface ChatMemoryDbService {
    void batchInsertIncrement(String convId, List<CustomChatMemoryDTO> dtoList);
    List<ChatMemoryGroupVO> getConversationGroupByDay(String conversationId);
}
