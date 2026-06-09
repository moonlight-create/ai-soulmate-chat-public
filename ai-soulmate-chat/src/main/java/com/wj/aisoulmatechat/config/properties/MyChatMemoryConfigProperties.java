package com.wj.aisoulmatechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "my.memory")
@Data
public class MyChatMemoryConfigProperties {
    private int expireDay = 0;
    private int maxContextMessage = 6;
    private int maxBatchSaveMessage = 6;
}
