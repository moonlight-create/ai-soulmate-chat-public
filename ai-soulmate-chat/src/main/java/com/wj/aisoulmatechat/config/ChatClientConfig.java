package com.wj.aisoulmatechat.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.wj.aisoulmatechat.config.customadvisors.RepeatQuestionLimitAdvisor;
import com.wj.aisoulmatechat.tools.AiUseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    //    @Bean
//    public ChatClient chatClient(DashScopeChatModel dashScopeChatModel,MessageWindowChatMemory messageWindowChatMemory){
//        PromptChatMemoryAdvisor advisor = PromptChatMemoryAdvisor.builder(messageWindowChatMemory).build();
//        return ChatClient.builder(dashScopeChatModel)
//                .defaultAdvisors(advisor)
//                .build();
//    }

    /**
     * 日志记录器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    @Bean
    public ChatClient chatClient(DashScopeChatModel dashScopeChatModel
                                ,MessageChatMemoryAdvisor messageChatMemoryAdvisor
                                , AiUseTools aiUseTools
                                , @Qualifier("loggerAdvisor") Advisor loggerAdvisor
                                , QuestionAnswerAdvisor qaAdvisor
                                , RepeatQuestionLimitAdvisor repeatQuestionLimitAdvisor
                                ,SafeGuardAdvisor safeGuardAdvisor
                                ,@Qualifier("mijiaMcpTools")ToolCallbackProvider toolCallbackProvider
    ){
        return ChatClient.builder(dashScopeChatModel)
                .defaultTools(aiUseTools)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(
                    safeGuardAdvisor
                    ,repeatQuestionLimitAdvisor
                    ,messageChatMemoryAdvisor
                    ,loggerAdvisor
                    ,qaAdvisor)
                .build();
    }

//    @Bean
//    public ChatClient chatClient(ChatClient.Builder builder, MessageWindowChatMemory chatMemory) {
//        // Spring自动注入的builder已经绑定项目唯一DashScopeChatModel，不用手动传model
//        return builder
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build();
//    }

}
