package com.wj.aisoulmatechat.config.customadvisors;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomDealPromptConfig {
    @Bean
    public RepeatQuestionLimitAdvisor repeatQuestionLimitAdvisor(ChatMemory chatMemory){
        return new RepeatQuestionLimitAdvisor(chatMemory);
    }
}
