package com.wj.aisoulmatechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "my.safeguard")
public class AiSafeguardProperties {
    private List<String> sensitiveWords;
    private String failureResponse;
}
