package com.wj.aisoulmatechat.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RagConfig {

    // RAG全局知识库专用
    @Bean
    @Primary
    public TextSplitter knowledgeSplitter(){
        return TokenTextSplitter.builder()
                .withChunkSize(900)
                .withMinChunkSizeChars(150)
                .build();
    }

    //聊天摘要专用
    @Bean("chatSplitter")
    public TokenTextSplitter chatTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(700)
                .withMinChunkSizeChars(120)
                .build();
    }

    @Bean
    public QuestionAnswerAdvisor qaAdvisor(VectorStore vectorStore) {
        SearchRequest sr = SearchRequest.builder().topK(4).similarityThreshold(0.1d).build();
        PromptTemplate pt = PromptTemplate.builder()
                .template("""
                        【知识库内容】
                        {question_answer_context}
                        用户问题：{query}
                        有资料依托资料回答，无资料正常聊天，不许说看不懂。
                        """).build();
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(sr)
                .promptTemplate(pt)
                .order(0)
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor soulmateRagAdvisor(VectorStore vectorStore) {
        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(4)
                .similarityThreshold(0.1d)
                .build();

        PromptTemplate ragPrompt = PromptTemplate.builder()
                .template("""
                    【知识库内容】
                    {context}
                    用户问题：{query}
                    有资料依托资料回答，无资料正常聊天，不许说看不懂。
                    """)
                .build();

        PromptTemplate emptyTpl = PromptTemplate.builder()
                .template("{query}")
                .build();

        //2、组装RAG增强器，可配置是否允许空上下文（原生默认空上下文拒绝回答）
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                // 允许召回文档为空时继续让模型回答
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .promptTemplate(ragPrompt)
                        .emptyContextPromptTemplate(emptyTpl)
                        .build())
                // 可选：增加查询重写（模糊问句自动优化检索词）
                //.queryTransformers(RewriteQueryTransformer.builder(ChatClient.builder(chatModel)).build())
                .build();

        return ragAdvisor;
    }

}
