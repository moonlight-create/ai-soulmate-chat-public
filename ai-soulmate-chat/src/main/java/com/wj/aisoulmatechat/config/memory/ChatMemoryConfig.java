package com.wj.aisoulmatechat.config.memory;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.wj.aisoulmatechat.config.customadvisors.RepeatQuestionLimitAdvisor;
import com.wj.aisoulmatechat.config.properties.MyChatMemoryConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatMemoryConfig {
    private final MyChatMemoryConfigProperties myChatMemoryConfigProperties;

//    @Bean
//    public ChatMemoryRepository inMemoryRepo(){
//        return new InMemoryChatMemoryRepository();
//    }

    @Bean
    public ChatMemory chatMemory(@Qualifier("customChatMemoryRepository") ChatMemoryRepository chatMemoryRepository){
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository)
//                .maxMessages(6)
//                .build();

        //自定义消息记忆，不删除redis会话，只控制传给大模型的对话条数
        return CustomFullWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxLoadMessages(myChatMemoryConfigProperties.getMaxContextMessage())
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory){
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

//    @Bean
//    public ChatMemory chatMemory(CustomChatMemoryRepo customChatMemoryRepo){
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(customChatMemoryRepo)
//                .maxMessages(30)
//                .build();
//    }

}
