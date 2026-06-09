package com.wj.aisoulmatechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "my.server")
@Data
public class MyServerConfigProperties {
    private String ip;
}
