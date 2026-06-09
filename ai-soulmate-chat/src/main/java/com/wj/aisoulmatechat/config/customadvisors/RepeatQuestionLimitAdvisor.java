package com.wj.aisoulmatechat.config.customadvisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;

public class RepeatQuestionLimitAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String REPEAT_TIP = "\n【强制约束：该问题历史已有回复，本次全部更换对话动作、语句】";
    private static final int DEFAULT_ORDER = -1;

    private final ChatMemory chatMemory;
    private final int order;

    // 构造器1：默认order=-1，优先于记忆Advisor执行
    public RepeatQuestionLimitAdvisor(ChatMemory chatMemory) {
        this(chatMemory, DEFAULT_ORDER);
    }

    // 构造器2：自定义order
    public RepeatQuestionLimitAdvisor(ChatMemory chatMemory, int order) {
        this.chatMemory = chatMemory;
        this.order = order;
    }

    // 对外Builder，对齐官方SafeGuard设计
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 取原始用户内容：单条用户消息
        Prompt originPrompt = chatClientRequest.prompt();
        UserMessage rawUserMsg = originPrompt.getUserMessage();
        String rawUserInput = rawUserMsg.getText().trim();

        // 从context获取会话ID
        String convId = (String) chatClientRequest.context().get(ChatMemory.CONVERSATION_ID);
        if(convId == null || convId.isBlank()){
            return callAdvisorChain.nextCall(chatClientRequest);
        }

        // 读取Redis里的历史对话
        List<Message> historyMsgList = chatMemory.get(convId);

        // 历史查重
        boolean isRepeat = historyMsgList.stream()
                .filter(msg -> MessageType.USER == msg.getMessageType())
                .anyMatch(msg -> msg.getText().trim().equals(rawUserInput));

        if (!isRepeat) {
            // 无重复，原请求直接放行
            return callAdvisorChain.nextCall(chatClientRequest);
        }

        // 重复提问：仅修改本次用户消息内容
        String llmInputText = rawUserInput + REPEAT_TIP;
        // 构造新Prompt（当前Prompt只有一条用户消息，直接替换）
        Prompt newPrompt = originPrompt.augmentUserMessage(llmInputText);

        // 构造新Request
        ChatClientRequest newRequest = ChatClientRequest.builder()
                .prompt(newPrompt)
                .context(chatClientRequest.context())
                .build();

        return callAdvisorChain.nextCall(newRequest);
    }

    // SSE流式调用
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
        // 取原始用户内容：单条用户消息
        Prompt originPrompt = chatClientRequest.prompt();
        UserMessage rawUserMsg = originPrompt.getUserMessage();
        String rawUserInput = rawUserMsg.getText().trim();

        // 从context获取会话ID
        String convId = (String) chatClientRequest.context().get(ChatMemory.CONVERSATION_ID);
        if (convId == null || convId.isBlank()) {
            // 无会话ID直接放行原请求
            return streamAdvisorChain.nextStream(chatClientRequest);
        }

        // 读取Redis里的历史对话
        List<Message> historyMsgList = chatMemory.get(convId);

        //去除最后一条数据（chatMemory已经执行，去除本次的用户提问数据）
        if (!CollectionUtils.isEmpty(historyMsgList)) {
            historyMsgList.remove(historyMsgList.size() - 1);
        }

        // 历史查重
        boolean isRepeat = historyMsgList.stream()
                .filter(msg -> MessageType.USER == msg.getMessageType())
                .anyMatch(msg -> msg.getText().trim().equals(rawUserInput));

        if (!isRepeat) {
            // 无重复，原请求直接放行
            return streamAdvisorChain.nextStream(chatClientRequest);
        }

        // 重复提问：拼接约束，构建新Request
        String llmInputText = rawUserInput + REPEAT_TIP;
        Prompt newPrompt = originPrompt.augmentUserMessage(llmInputText);

        ChatClientRequest newRequest = ChatClientRequest.builder()
                .prompt(newPrompt)
                .context(chatClientRequest.context())
                .build();

        return streamAdvisorChain.nextStream(newRequest);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public static final class Builder {
        private ChatMemory chatMemory;
        private int order = DEFAULT_ORDER;

        private Builder() {}

        public Builder chatMemory(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public RepeatQuestionLimitAdvisor build() {
            return new RepeatQuestionLimitAdvisor(this.chatMemory, this.order);
        }
    }
}
