package com.wj.aisoulmatechat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ToolConfig {
    /**
     * 初始化 RestClient，指定 Open-Meteo 基础地址
     */
    @Bean
    public RestClient openMeteoRestClient() {
        return RestClient.builder().build();
    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper();
//    }

}
