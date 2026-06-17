package com.wj.aisoulmatechat.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class VectoryDBConfig {

    @Bean
    @Primary
    public DashScopeEmbeddingModel dashScopeEmbeddingModel(@Value("${spring.ai.dashscope.api-key}") String apiKey){
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        DashScopeEmbeddingOptions options = DashScopeEmbeddingOptions.builder()
                .withModel("text-embedding-v1")
                .withTextType("document")
                .withDimensions(1536)
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

    // 1. 读取yml Milvus连接参数，创建客户端
//    @Bean(destroyMethod = "close")
//    public MilvusServiceClient milvusServiceClient(
//            @Value("${spring.ai.vectorstore.milvus.client.host}") String host,
//            @Value("${spring.ai.vectorstore.milvus.client.port}") int port,
//            @Value("${spring.ai.vectorstore.milvus.client.username}") String username,
//            @Value("${spring.ai.vectorstore.milvus.client.password}") String password
//        ) {
//        ConnectParam connectParam = ConnectParam.newBuilder()
//            .withHost(host)
//            .withPort(port)
//            .withAuthorization(username, password)
//            .build();
//        return new MilvusServiceClient(connectParam);
//    }


    // 内存向量库
//    @Bean
//    public VectorStore vectorStore(@Qualifier("dashScopeEmbeddingModel") EmbeddingModel embeddingModel){
//        return SimpleVectorStore.builder(embeddingModel).build();
//    }

//    @Bean
//    @Primary
//    public VectorStore myVectorStore(
//            MilvusServiceClient milvusServiceClient,
//            @Qualifier("dashScopeEmbeddingModel") EmbeddingModel embeddingModel,
//            MilvusVectorStoreProperties milvusProps
//    ) {
//        return MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
//                .databaseName(milvusProps.getDatabaseName())
//                .collectionName(milvusProps.getCollectionName())
//                .embeddingDimension(milvusProps.getEmbeddingDimension())
//                .initializeSchema(true) // 手动开启自动建标准Schema
//                .build();
//    }

}
