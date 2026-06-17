package com.wj.aisoulmatechat.config.customadvisors;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EvaluatorCheckAdvisor implements CallAdvisor, StreamAdvisor {
    private static final String RAG_DOC_KEY = "rag_document_context";
    private static final String PARAM_USER_QUERY = "userPrompt";
    private static final String PARAM_EVAL_RETRY = "eval_retry_count";
    private static final int MAX_RETRY = 2;

    private final ChatModel judgeChatModel;
    private final RelevancyEvaluator relevancyEvaluator;
    private final FactCheckingEvaluator factCheckingEvaluator;

    // 构造注入模型作为评测
    public EvaluatorCheckAdvisor(DashScopeChatModel chatModel) {
        this.judgeChatModel = chatModel;
        // 相关性评测器
        this.relevancyEvaluator = RelevancyEvaluator.builder()
                .chatClientBuilder(ChatClient.builder(judgeChatModel))
                .build();
        // 事实幻觉校验评测器
        this.factCheckingEvaluator = FactCheckingEvaluator.forBespokeMinicheck(ChatClient.builder(judgeChatModel));
    }

    // 拿到AI回答后评测
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);

        Map<String, Object> context = request.context();
        String userQuery = (String) context.get(PARAM_USER_QUERY);
        List<Document> ragDocs = (List<Document>) context.get(RAG_DOC_KEY);

        if (userQuery == null || userQuery.isBlank()) {
            return response;
        }

        String aiAnswer = response.chatResponse().getResult().getOutput().getText();
        List<Document> dataList = ragDocs == null ? List.of() : ragDocs;
        EvaluationRequest evalReq = new EvaluationRequest(userQuery, dataList, aiAnswer);

        // 执行评测不变
        EvaluationResponse relResp = relevancyEvaluator.evaluate(evalReq);
        EvaluationResponse factResp = factCheckingEvaluator.evaluate(evalReq);

        boolean pass = relResp.isPass() && factResp.isPass();
        if (pass) {
            return response;
        }

        // 评测不通过，处理重试逻辑
        int retryCount = (Integer) context.getOrDefault(PARAM_EVAL_RETRY, 0);
        if (retryCount >= MAX_RETRY) {
            // 达到上限，追加警告文本直接返回
            String warning = String.format(
                    "\n[提示] 回答存在瑕疵：相关性问题：%s；事实准确性问题：%s",
                    relResp.getFeedback(), factResp.getFeedback()
            );
            String finalText = aiAnswer + warning;
            ChatResponse oldChatResp = response.chatResponse();
            Generation oldGen = oldChatResp.getResult();
            AssistantMessage newAssistantMsg = new AssistantMessage(finalText);
            Generation newGen = new Generation(newAssistantMsg, oldGen.getMetadata());
            ChatResponse newChatResponse = new ChatResponse(List.of(newGen), oldChatResp.getMetadata());

            // mutate替换内部chatResponse并返回
            return response.mutate()
                    .chatResponse(newChatResponse)
                    .build();
        }

        // 重试计数+1，构造修正提示重新调用完整链路
        context.put(PARAM_EVAL_RETRY, retryCount + 1);
        String fixPrompt = String.format("""
                你上一轮回答存在缺陷：
                1. 相关性问题：%s
                2. 事实问题：%s
                请严格基于提供的参考资料重新准确回答用户原始问题，禁止编造无依据内容。
                用户提问：%s
                """, relResp.getFeedback(), factResp.getFeedback(), userQuery);

        Prompt fixUserPrompt = new Prompt(fixPrompt);
        ChatClientRequest retryRequest = request.mutate()
                .prompt(fixUserPrompt)
                .build();
        return chain.nextCall(retryRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        Flux<ChatClientResponse> originFlux = chain.nextStream(request);
        Map<String, Object> context = request.context();
        String userQuery = (String) context.get(PARAM_USER_QUERY);
        List<Document> ragDocs = (List<Document>) context.get(RAG_DOC_KEY);

        if (userQuery == null || userQuery.isBlank()) {
            return originFlux;
        }

        return originFlux.collectList()
                .flatMapMany(responseList -> {
                    // 1. 拼接完整回答文本
                    String fullAnswer = concatStreamChunk(responseList);

                    if (fullAnswer.isBlank()) {
                        return Flux.fromIterable(responseList);
                    }

                    // 执行评测
                    List<Document> dataList = ragDocs == null ? List.of() : ragDocs;
                    if (dataList.isEmpty()) {
                        return Flux.fromIterable(responseList);
                    }
                    EvaluationRequest evalReq = new EvaluationRequest(userQuery, dataList, fullAnswer);
                    EvaluationResponse relResp = relevancyEvaluator.evaluate(evalReq);
                    EvaluationResponse factResp = factCheckingEvaluator.evaluate(evalReq);
                    boolean pass = relResp.isPass() && factResp.isPass();

                    // 评测不通过：打印监控日志 + 拼接提示分片
                    Flux<ChatClientResponse> finalFlux = Flux.fromIterable(responseList);
                    if (!pass) {
                        String feedback = String.format("流式对话评测不通过 | 用户提问：%s | 相关性问题：%s | 事实问题：%s",
                                userQuery, relResp.getFeedback(), factResp.getFeedback());
                        log.warn(feedback);

                        String tipText = String.format("\n\n【提示】本次回答存在内容瑕疵：%s", relResp.getFeedback());
                        ChatClientResponse tipResponse = buildTipChatClientResponse(tipText, context);
                        finalFlux = finalFlux.concatWith(Flux.just(tipResponse));
                    }
                    return finalFlux;
                });
    }

    /**
     * 拼接流式所有分片文本
     */
    private String concatStreamChunk(List<ChatClientResponse> responseList) {
        StringBuilder sb = new StringBuilder();
        for (ChatClientResponse resp : responseList) {
            ChatResponse chatResp = resp.chatResponse();
            if (chatResp == null || chatResp.getResult() == null) {
                continue;
            }
            String chunk = chatResp.getResult().getOutput().getText();
            if (chunk != null) {
                sb.append(chunk);
            }
        }
        return sb.toString();
    }

    /**
     * 构造末尾风险提示ChatClientResponse
     */
    private ChatClientResponse buildTipChatClientResponse(String tipText, Map<String, Object> context) {
        AssistantMessage tipMsg = new AssistantMessage(tipText);
        Generation gen = new Generation(tipMsg);
        ChatResponse chatResponse = new ChatResponse(List.of(gen));

        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(context)
                .build();
    }

    @Override
    public String getName() {
        return "EvaluatorCheckAdvisor";
    }

    /**
     * 在最后执行
     * @return
     */
    @Override
    public int getOrder() {
        return 100;
    }
}
