package com.wj.aisoulmatechat.config.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 全量永久存入Redis，仅读取时内存截取近N条发给大模型
 * 对标官方MessageWindowChatMemory结构，存储不裁剪、不覆写删除历史
 */
public final class CustomFullWindowChatMemory implements ChatMemory {

    // 传给LLM最大携带消息条数
    private static final int DEFAULT_MAX_LOAD_MSG = 10;

    private final ChatMemoryRepository chatMemoryRepository;
    private final int maxLoadMessages;

    private CustomFullWindowChatMemory(ChatMemoryRepository chatMemoryRepository, int maxLoadMessages) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxLoadMessages > 0, "maxLoadMessages must be greater than 0");
        this.chatMemoryRepository = chatMemoryRepository;
        this.maxLoadMessages = maxLoadMessages;
    }

    /**
     * 入库：先查全量历史 + 拼接新消息再入库 → Redis全量留存，不会丢历史只剩单条
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        //取出Redis全部旧数据
        List<Message> oldAll = chatMemoryRepository.findByConversationId(conversationId);
        //旧+新合并
        List<Message> saveTotal = new ArrayList<>(oldAll);
        saveTotal.addAll(messages);
        //合并后的完整列表入库
        this.chatMemoryRepository.saveAll(conversationId, saveTotal);
    }

    /**
     * 查询全量历史，内存阶段截取末尾N条给大模型，Redis数据完整保留
     */
    @Override
    public List<Message> get(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        List<Message> allMessages = this.chatMemoryRepository.findByConversationId(conversationId);

        if (allMessages.size() <= maxLoadMessages) {
            return allMessages;
        }

//        List<Message> messages = allMessages.subList(allMessages.size() - maxLoadMessages, allMessages.size());

        // 只截取末尾N条，早期历史仍完整在Redis
        return allMessages.subList(allMessages.size() - maxLoadMessages, allMessages.size());
    }

    /**
     * 保留原生clear：仅手动调用时删除会话全量数据
     */
    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        this.chatMemoryRepository.deleteByConversationId(conversationId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ChatMemoryRepository chatMemoryRepository;
        private int maxLoadMessages = DEFAULT_MAX_LOAD_MSG;

        private Builder() {}

        public Builder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
            this.chatMemoryRepository = chatMemoryRepository;
            return this;
        }

        /** 配置每次传给LLM的最大消息条数(只影响入参，不影响Redis存储) */
        public Builder maxLoadMessages(int maxLoadMessages) {
            this.maxLoadMessages = maxLoadMessages;
            return this;
        }

        public CustomFullWindowChatMemory build() {
            if (this.chatMemoryRepository == null) {
                this.chatMemoryRepository = new InMemoryChatMemoryRepository();
            }
            return new CustomFullWindowChatMemory(this.chatMemoryRepository, this.maxLoadMessages);
        }
    }
}
