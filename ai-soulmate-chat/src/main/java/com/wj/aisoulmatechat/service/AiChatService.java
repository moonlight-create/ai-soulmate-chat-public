package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.vo.ChatMemoryGroupVO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatService {
    String simpleChat(Long userId, String prompt);

    Flux<String> streamChat(Long userId, Long soulmateId, String userPrompt);

    String getFirstOpeningMsg(Long userId, Long soulmateId);

    List<ChatMemoryGroupVO> getMemoryGroupByDay(Long userId, Long soulmateId);
}
