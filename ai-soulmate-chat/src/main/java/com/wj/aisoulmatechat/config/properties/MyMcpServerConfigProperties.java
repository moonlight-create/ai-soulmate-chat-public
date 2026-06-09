package com.wj.aisoulmatechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "my.mcp")
@Data
public class MyMcpServerConfigProperties {
    private MijiaProps mijia;

    @Data
    public static class MijiaProps {
        private String path;
        private String mijiaApiUrl;
        private String mijiaToken;
        private Long mijiaInitTimeoutS;
        private Long mijiaTimeoutS;
        private Boolean mijiaSchemaCache;
    }

}
