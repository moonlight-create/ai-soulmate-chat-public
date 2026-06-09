package com.wj.aisoulmatechat.config.safe;

import com.wj.aisoulmatechat.config.properties.AiSafeguardProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@RequiredArgsConstructor
public class SafeGuardConfig {
    private final AiSafeguardProperties aiSafeguardProperties;

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor(){
       return new SafeGuardAdvisor(
           aiSafeguardProperties.getSensitiveWords(),
           aiSafeguardProperties.getFailureResponse(),
               //保证在会话记忆前边（会话记忆是Ordered.HIGHEST_PRECEDENCE + 1000）
               Ordered.HIGHEST_PRECEDENCE + 500
       );
    }
}
