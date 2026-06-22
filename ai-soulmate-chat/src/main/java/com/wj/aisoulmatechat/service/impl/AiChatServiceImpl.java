package com.wj.aisoulmatechat.service.impl;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wj.aisoulmatechat.common.util.ConversationUtil;
import com.wj.aisoulmatechat.config.memory.CustomFullWindowChatMemory;
import com.wj.aisoulmatechat.config.properties.BasePromptConfigProperties;
import com.wj.aisoulmatechat.service.AiChatService;
import com.wj.aisoulmatechat.service.ChatMemoryDbService;
import com.wj.aisoulmatechat.service.SoulmateService;
import com.wj.aisoulmatechat.vo.ChatMemoryGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {
    private final SoulmateService soulmateService;
    private final BasePromptConfigProperties basePromptConfigProperties;
    private final ChatClient chatClient;
    private final CustomFullWindowChatMemory messageWindowChatMemory;
    private final ChatMemoryDbService chatMemoryService;

    @Autowired
    @Qualifier("dashscopeChatModel")
    private ChatModel dashScopeChatModel;

    @Override
    public String simpleChat(Long userId, String prompt) {
        return dashScopeChatModel.call(prompt);
    }

    @Override
    public Flux<String> streamChat(Long userId, Long soulmateId, String userPrompt) {
        String convId = ConversationUtil.buildSoulmateConvId(userId,soulmateId);
        // 历史数据入库向量数据库
        soulmateService.checkAndBuildHistorySummary(convId);
        // 拼接人设
        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());
        // 向量过滤
//        String filterExpr = String.format("conversationId == \"%s\"", convId);
        String filterExpr = String.format(
                "conversationId == \"%s\" && soulmateId == %d && userId == %d",
                convId, soulmateId, userId
        );

        return chatClient.prompt()
                .system(fullSysPrompt)
                .user(userPrompt)
                .toolContext(Map.of("soulmateId",soulmateId,"userId",userId,"convId",convId))
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, convId)
                        .param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpr)
                        .param(RetrievalRerankAdvisor.FILTER_EXPRESSION, filterExpr)
                        .param("userPrompt", userPrompt)
                )
                .stream()
                .content();
    }

    @Override
    public String getFirstOpeningMsg(Long userId, Long soulmateId) {
        String convId = ConversationUtil.buildSoulmateConvId(userId,soulmateId);
        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());
        String finalPrompt = fullSysPrompt + "\n" + basePromptConfigProperties.getFirst();

        DashScopeChatOptions firstOptions = DashScopeChatOptions.builder()
                .withTemperature(1.8d)
                .withTopP(0.9d)
                .build();

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(finalPrompt),
                new UserMessage(basePromptConfigProperties.getHello())
        ),firstOptions);
        ChatResponse resp = dashScopeChatModel.call(prompt);
        String aiAnswer = resp.getResult().getOutput().getText();

        // 存入聊天记忆
        List<Message> saveMsgList = List.of(new AssistantMessage(aiAnswer));
        messageWindowChatMemory.add(convId,saveMsgList);
        return aiAnswer;
    }

    @Override
    public List<ChatMemoryGroupVO> getMemoryGroupByDay(Long userId, Long soulmateId) {
        String convId = ConversationUtil.buildSoulmateConvId(userId,soulmateId);
        return chatMemoryService.getConversationGroupByDay(convId);
    }
}
