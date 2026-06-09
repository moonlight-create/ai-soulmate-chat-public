package com.wj.aisoulmatechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration.class
})
@MapperScan("com.wj.aisoulmatechat.mapper")
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AiSoulmateChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiSoulmateChatApplication.class, args);
    }

}
