package com.wj.aisoulmatechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "my.prompt")
@Data
public class BasePromptConfigProperties {
    private String base;
    private String first;
    private String hello;
    private String summary;
}
