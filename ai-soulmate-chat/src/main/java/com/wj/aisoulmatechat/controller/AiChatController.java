package com.wj.aisoulmatechat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wj.aisoulmatechat.config.memory.CustomFullWindowChatMemory;
import com.wj.aisoulmatechat.config.properties.BasePromptConfigProperties;
import com.wj.aisoulmatechat.dto.UserPromptDTO;
import com.wj.aisoulmatechat.security.LoginUser;
import com.wj.aisoulmatechat.service.SoulmateService;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.vectorstore.SearchRequest;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class AiChatController {

    private final ChatModel dashScopeChatModel;
    private final SoulmateService soulmateService;
    private final BasePromptConfigProperties basePromptConfigProperties;
    private final ChatClient chatClient;
    private final CustomFullWindowChatMemory messageWindowChatMemory;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
//    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;

    public AiChatController(@Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,SoulmateService soulmateService,BasePromptConfigProperties basePromptConfigProperties,ChatClient chatClient,CustomFullWindowChatMemory messageWindowChatMemory,QuestionAnswerAdvisor questionAnswerAdvisor) {
        this.dashScopeChatModel = dashscopeChatModel;
        this.soulmateService = soulmateService;
        this.basePromptConfigProperties = basePromptConfigProperties;
        this.chatClient = chatClient;
        this.messageWindowChatMemory = messageWindowChatMemory;
        this.questionAnswerAdvisor = questionAnswerAdvisor;
//        this.retrievalAugmentationAdvisor = retrievalAugmentationAdvisor;
    }

    @SneakyThrows
    @PostMapping("/ai-chat")
    public String aiChat(@RequestParam("prompt") String userPrompt) {
        return dashScopeChatModel.call(userPrompt);
    }

    @SneakyThrows
    @PostMapping(value = "/ai-chat-stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiChatStream(@RequestBody UserPromptDTO userPromptDto, Authentication auth) {
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        Long userId = loginUser.getUser().getId();
        Long soulmateId = userPromptDto.getSoulmateId();
        String userPrompt = userPromptDto.getUserPrompt();
        String convId = "soulmate:memory:" + userId + ":" + soulmateId;
//        String convId = "soulmate_memory_" + userId + "_" + soulmateId;

        //历史数据入库向量数据库
        soulmateService.checkAndBuildHistorySummary(convId);
        //拼接基础+动态人设
        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());

        // 向量数据库过滤条件
//        String filterExpr = String.format("userId == %d && soulmateId == %d", userId, soulmateId);
        String filterExpr = String.format("conversationId == '%s'", convId);

        // 构建过滤表达式
//        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
//        Filter.Expression filterExpression = filterExpressionBuilder.eq("conversationId", convId).build();

//        // 动态构建带过滤的 SearchRequest
//        SearchRequest dynamicSr = SearchRequest.builder()
//                .topK(4)
//                .similarityThreshold(0.35d)
//                .filterExpression(filterExpression)
//                .build();

        return chatClient.prompt()
                .system(fullSysPrompt)
                .user(userPrompt)
                .toolContext(Map.of("soulmateId",soulmateId,"userId",userId,"convId",convId))
                .advisors(spec -> spec
                        .advisors(questionAnswerAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, convId)
//                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, dynamicSr)
                        .param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpr)
                                .param("userPrompt", userPrompt)
                )
//                .advisors(a->a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION,filterExpr))
//                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,convId))
                .stream()
                .content();
    }

    @SneakyThrows
    @GetMapping("/get-first-msg")
    @ResponseBody
    public String getFirstMsg(@RequestParam("soulmateId") Long soulmateId,Authentication auth) {
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        Long userId = loginUser.getUser().getId();
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

        return aiAnswer;
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

}
