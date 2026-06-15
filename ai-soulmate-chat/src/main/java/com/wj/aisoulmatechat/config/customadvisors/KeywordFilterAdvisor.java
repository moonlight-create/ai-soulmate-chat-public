package com.wj.aisoulmatechat.config.customadvisors;

import com.wj.aisoulmatechat.util.IkKeywordUtil;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeywordFilterAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String RAG_DOC_KEY = "rag_document_context";
    private static final String META_KEYWORD = "keyword";
    private static final String PARAM_USER_QUERY = "userPrompt";

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);

        String userQuery = (String) request.context().get(PARAM_USER_QUERY);
        if (userQuery == null || userQuery.isBlank()) {
            return response;
        }

        String queryKeywords = IkKeywordUtil.extractKeywords(userQuery);
        if (queryKeywords.isBlank()) {
            return response;
        }

        Map<String, Object> contextMap = request.context();
        List<Document> originDocs = (List<Document>) contextMap.get(RAG_DOC_KEY);
        if (originDocs == null || originDocs.isEmpty()) {
            return response;
        }

        List<Document> filterDocs = originDocs.stream()
                .filter(doc -> {
                    Map<String, Object> meta = doc.getMetadata();
                    Object docKeywordObj = meta.get(META_KEYWORD);
                    String docKeywords = docKeywordObj instanceof String ? (String) docKeywordObj : "";
                    return IkKeywordUtil.isMatch(docKeywords, queryKeywords);
                })
                .collect(Collectors.toList());

        contextMap.put(RAG_DOC_KEY, filterDocs);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        String userQuery = (String) request.context().get(PARAM_USER_QUERY);
        if (userQuery == null || userQuery.isBlank()) {
            return chain.nextStream(request);
        }

        String queryKeywords = IkKeywordUtil.extractKeywords(userQuery);
        if (queryKeywords.isBlank()) {
            return chain.nextStream(request);
        }

        Map<String, Object> contextMap = request.context();
        List<Document> originDocs = (List<Document>) contextMap.get(RAG_DOC_KEY);
        if (originDocs == null || originDocs.isEmpty()) {
            return chain.nextStream(request);
        }

        List<Document> filterDocs = originDocs.stream()
                .filter(doc -> {
                    Map<String, Object> meta = doc.getMetadata();
                    Object docKeywordObj = meta.get(META_KEYWORD);
                    String docKeywords = docKeywordObj instanceof String ? (String) docKeywordObj : "";
                    return IkKeywordUtil.isMatch(docKeywords, queryKeywords);
                })
                .collect(Collectors.toList());

        contextMap.put(RAG_DOC_KEY, filterDocs);
        return chain.nextStream(request);
    }

    @Override
    public String getName() {
        return "KeywordFilterAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}

