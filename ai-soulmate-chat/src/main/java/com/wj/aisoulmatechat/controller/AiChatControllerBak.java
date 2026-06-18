package com.wj.aisoulmatechat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wj.aisoulmatechat.common.result.Result;
import com.wj.aisoulmatechat.config.memory.CustomFullWindowChatMemory;
import com.wj.aisoulmatechat.config.properties.BasePromptConfigProperties;
import com.wj.aisoulmatechat.dto.UserPromptDTO;
import com.wj.aisoulmatechat.service.ChatMemoryDbService;
import com.wj.aisoulmatechat.service.SoulmateService;
import com.wj.aisoulmatechat.util.SecurityUserUtil;
import com.wj.aisoulmatechat.vo.ChatMemoryGroupVO;
import lombok.SneakyThrows;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

//@RestController
@RequestMapping("/chat")
public class AiChatControllerBak {

    private final ChatModel dashScopeChatModel;
    private final SoulmateService soulmateService;
    private final BasePromptConfigProperties basePromptConfigProperties;
    private final ChatClient chatClient;
    private final CustomFullWindowChatMemory messageWindowChatMemory;
    private final ChatMemoryDbService chatMemoryService;

    public AiChatControllerBak(@Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel, SoulmateService soulmateService, BasePromptConfigProperties basePromptConfigProperties, ChatClient chatClient, CustomFullWindowChatMemory messageWindowChatMemory, ChatMemoryDbService chatMemoryService) {
        this.dashScopeChatModel = dashscopeChatModel;
        this.soulmateService = soulmateService;
        this.basePromptConfigProperties = basePromptConfigProperties;
        this.chatClient = chatClient;
        this.messageWindowChatMemory = messageWindowChatMemory;
        this.chatMemoryService = chatMemoryService;
    }

    @SneakyThrows
    @PostMapping("/ai-chat")
    public Result<String> aiChat(@RequestParam("prompt") String userPrompt) {
        String res = dashScopeChatModel.call(userPrompt);
        return Result.ok(res);
    }

    @SneakyThrows
    @PostMapping(value = "/ai-chat-stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiChatStream(@RequestBody UserPromptDTO userPromptDto) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        Long soulmateId = userPromptDto.getSoulmateId();
        String userPrompt = userPromptDto.getUserPrompt();
        String convId = "soulmate:memory:" + userId + ":" + soulmateId;
//        String convId = "soulmate_memory_" + userId + "_" + soulmateId;

        //历史数据入库向量数据库
        soulmateService.checkAndBuildHistorySummary(convId);
        //拼接基础+动态人设
        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());

        // 向量数据库过滤条件
//        String filterExpr = String.format("conversationId == '%s'", convId);
        String filterExpr = String.format("conversationId == \"%s\"", convId);

        return chatClient.prompt()
                .system(fullSysPrompt)
                .user(userPrompt)
                .toolContext(Map.of("soulmateId",soulmateId,"userId",userId,"convId",convId))
                .advisors(spec -> spec
//                        .advisors(questionAnswerAdvisor)
                                .param(ChatMemory.CONVERSATION_ID, convId)
//                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpr)
                                .param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpr)
                                .param("userPrompt", userPrompt)
                )
                .stream()
                .content();
    }

    @SneakyThrows
    @GetMapping("/get-first-msg")
    public Result<String> getFirstMsg(@RequestParam("soulmateId") Long soulmateId) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        String convId = "soulmate:memory:" + userId + ":" + soulmateId;
        //拼接基础+动态人设
        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());
        String finalPrompt = fullSysPrompt + "\n" +basePromptConfigProperties.getFirst();

        //开场白高温度，更灵活
        DashScopeChatOptions firstOptions = DashScopeChatOptions.builder()
                .withTemperature(1.8d)
                .withTopP(0.9d)
                .build();

        // 组装消息：System人设 + 用户指令
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(finalPrompt),
                new UserMessage(basePromptConfigProperties.getHello())
        ),firstOptions);
        ChatResponse resp = dashScopeChatModel.call(prompt);
        String aiAnswer = resp.getResult().getOutput().getText();

//        String aiAnswer = chatClient.prompt()
//                .system(finalPrompt)
//                .user("现在主动和我开启第一次聊天，生成一句开场白")
//                .options(firstOptions)
////                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,convId))
//                .call()
//                .content();

        //只存AI的开场白，手动
        List<Message> saveMsgList = List.of(new AssistantMessage(aiAnswer));
        messageWindowChatMemory.add(convId,saveMsgList);

        return Result.ok(aiAnswer);
    }


//    @SneakyThrows
//    @PostMapping(value = "/ai-chat-stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> aiChatStream(@RequestBody UserPromptDto userPromptDto,Authentication auth) {
//        LoginUser loginUser = (LoginUser) auth.getPrincipal();
//        Long userId = loginUser.getUser().getId();
//        Long soulmateId = userPromptDto.getSoulmateId();
//        String userPrompt = userPromptDto.getUserPrompt();
//        //拼接基础+动态人设
//        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());
//        Prompt prompt = new Prompt(List.of(
//                                    new SystemMessage(fullSysPrompt),
//                                    new UserMessage(userPrompt)
//        ));
//        return dashScopeChatModel.stream(prompt)
//                .map(chatResp -> chatResp.getResult().getOutput().getText());
//    }

    /**
     * 根据会话ID获取按天分组聊天记录
     * @param soulmateId 伴侣ID
     * @return 按天分组消息列表
     */
    @SneakyThrows
    @GetMapping("/memory/group_by_day")
    public Result<List<ChatMemoryGroupVO>> getChatGroupByDay(@RequestParam("soulmateId") Long soulmateId) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        String convId = "soulmate:memory:" + userId + ":" + soulmateId;
        List<ChatMemoryGroupVO> list = chatMemoryService.getConversationGroupByDay(convId);
        return Result.ok(list);
    }

}
