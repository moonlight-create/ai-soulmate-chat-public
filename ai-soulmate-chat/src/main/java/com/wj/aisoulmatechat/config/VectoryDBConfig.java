package com.wj.aisoulmatechat.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectoryDBConfig {

    @Bean
    public DashScopeEmbeddingModel dashScopeEmbeddingModel(@Value("${spring.ai.dashscope.api-key}") String apiKey){
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        DashScopeEmbeddingOptions options = DashScopeEmbeddingOptions.builder()
                .withModel("text-embedding-v1")
                .withTextType("document")
                .withDimensions(1024)
                .build();
        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED,options);
    }

//    @Bean
//    public VectorStore vectorStore(@Qualifier("dashScopeEmbeddingModel") EmbeddingModel embeddingModel){
//        //Chroma向量数据库
//        ChromaApi chromaApi = ChromaApi.builder()
////                .baseUrl("http://localhost:8000")
//                .build();
//        return ChromaVectorStore.builder(chromaApi,embeddingModel)
//                .collectionName("soulmate_memory")
//                .build();
//    }


    // 内存向量库
    @Bean
    public VectorStore vectorStore(@Qualifier("dashScopeEmbeddingModel") EmbeddingModel embeddingModel){
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
