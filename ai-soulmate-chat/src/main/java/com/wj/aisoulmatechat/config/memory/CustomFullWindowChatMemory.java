package com.wj.aisoulmatechat.config.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wj.aisoulmatechat.config.mq.RabbitMqConfig;
import com.wj.aisoulmatechat.entity.ChatMemoryIncrementMsg;
import com.wj.aisoulmatechat.util.RedisCacheUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 全量永久存入Redis，仅读取时内存截取近N条发给大模型
 * 对标官方MessageWindowChatMemory结构，存储不裁剪、不覆写删除历史
 */
public final class CustomFullWindowChatMemory implements ChatMemory {

    // 传给LLM最大携带消息条数
    private static final int DEFAULT_MAX_LOAD_MSG = 10;

    private final ChatMemoryRepository chatMemoryRepository;
    private final int maxLoadMessages;

    //Rocketmq异步入库聊天记录信息
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    public static final String CHAT_MEMORY_INCREMENT_TOPIC = "chat_memory_increment_topic";

    //Rabbitmq异步入库聊天记录信息
    @Autowired
    private RabbitTemplate rabbitTemplate;
    public static final String CHAT_MEMORY_EXCHANGE = RabbitMqConfig.CHAT_MEMORY_EXCHANGE;
    public static final String CHAT_MEMORY_ROUTING_KEY = RabbitMqConfig.ROUTING_KEY;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    private CustomFullWindowChatMemory(ChatMemoryRepository chatMemoryRepository, int maxLoadMessages) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxLoadMessages > 0, "maxLoadMessages must be greater than 0");
        this.chatMemoryRepository = chatMemoryRepository;
        this.maxLoadMessages = maxLoadMessages;
    }

    /**
     * 入库：先查全量历史 + 拼接新消息再入库 → Redis全量留存，不会丢历史只剩单条
     */
//    @Override
//    public void add(String conversationId, List<Message> messages) {
//        Assert.hasText(conversationId, "conversationId cannot be null or empty");
//        Assert.notNull(messages, "messages cannot be null");
//        Assert.noNullElements(messages, "messages cannot contain null elements");
//
//        //取出Redis全部旧数据
//        List<Message> oldAll = chatMemoryRepository.findByConversationId(conversationId);
//        //旧+新合并
//        List<Message> saveTotal = new ArrayList<>(oldAll);
//        saveTotal.addAll(messages);
//        //合并后的完整列表入库
//        this.chatMemoryRepository.saveAll(conversationId, saveTotal);
//
//        // ===================== MySQL 增量逻辑：只发送本次新增消息 =====================
//        CustomChatMemoryRepository repo = (CustomChatMemoryRepository) this.chatMemoryRepository;
//        // 仅转换本次新增的消息，不是全量会话
//        List<CustomChatMemoryDTO> incrementDtoList = messages.stream()
//                .map(msg -> {
//                            CustomChatMemoryDTO dto = repo.msgToDto(msg);
//                            // 生成单条消息唯一ID，用于数据库幂等
//                            dto.setMsgUuid(UUID.randomUUID().toString());
//                            return dto;
//                })
//                .toList();
//
//        // 封装MQ消息发送
////        sendIncrementMq(conversationId, incrementDtoList);
//        sendIncrementRabbitMq(conversationId, incrementDtoList);
//
//    }

//    @Override
//    public void add(String conversationId, List<Message> messages) {
//        Assert.hasText(conversationId, "conversationId cannot be null or empty");
//        Assert.notNull(messages, "messages cannot be null");
//        Assert.noNullElements(messages, "messages cannot contain null elements");
//
//        CustomChatMemoryRepository repo = (CustomChatMemoryRepository) this.chatMemoryRepository;
//        String uuidRedisKey = repo.getUuidRedisKey(conversationId);
//
//        // 1. 读取历史消息、历史每条消息对应的uuid列表
//        List<Message> oldAll = chatMemoryRepository.findByConversationId(conversationId);
//        List<String> oldUuidList = repo.getUuidList(uuidRedisKey);
//
//        // 2. 给本次新增消息批量生成唯一uuid
//        List<String> newUuidBatch = messages.stream()
//                .map(m -> UUID.randomUUID().toString())
//                .toList();
//
//        // 3. 合并全量消息、全量uuid（两条List长度永久相等，下标一一绑定）
//        List<Message> totalMsgList = new ArrayList<>(oldAll);
//        totalMsgList.addAll(messages);
//        List<String> totalUuidList = new ArrayList<>(oldUuidList);
//        totalUuidList.addAll(newUuidBatch);
//
//        // 4. 写入Redis：消息全量列表 + 完整uuid列表分开存储
//        repo.saveAllWithUuid(conversationId, totalMsgList, totalUuidList);
//
//        // 5. MQ增量发送，复用本次生成的uuid，和Redis、DB一致
//        List<CustomChatMemoryDTO> incrementDtoList = IntStream.range(0, messages.size())
//                .mapToObj(idx -> {
//                    Message msg = messages.get(idx);
//                    String uuid = newUuidBatch.get(idx);
//                    CustomChatMemoryDTO dto = repo.msgToDto(msg);
//                    dto.setMsgUuid(uuid);
//                    return dto;
//                })
//                .toList();
//
//        sendIncrementRabbitMq(conversationId, incrementDtoList);
//    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        CustomChatMemoryRepository repo = (CustomChatMemoryRepository) this.chatMemoryRepository;
        String msgKey = repo.getRedisKey(conversationId);

        List<Message> oldAll;
        List<String> oldUuidList = new ArrayList<>();
        try {
            // 直接读取Redis原始JSON，不使用findByConversationId（避免uuid丢失）
            List<String> oldJsonList = redisCacheUtil.listGetAll(msgKey);
            oldAll = new ArrayList<>();
            if (oldJsonList != null && !oldJsonList.isEmpty()) {
                for (String json : oldJsonList) {
                    CustomChatMemoryDTO dto = repo.getObjectMapper().readValue(json, CustomChatMemoryDTO.class);
                    oldAll.add(repo.dtoToMsg(dto));
                    oldUuidList.add(dto.getMsgUuid());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("读取历史会话消息解析失败", e);
        }

        // 本次新增消息批量生成uuid
        List<String> newUuidBatch = messages.stream()
                .map(m -> UUID.randomUUID().toString())
                .toList();

        // 合并全量消息、全量uuid
        List<Message> totalMsgList = new ArrayList<>(oldAll);
        totalMsgList.addAll(messages);
        List<String> totalUuidList = new ArrayList<>(oldUuidList);
        totalUuidList.addAll(newUuidBatch);

        // 存入Redis
        repo.saveAllWithUuidNoExtraKey(conversationId, totalMsgList, totalUuidList);

        // MQ入库
        List<CustomChatMemoryDTO> incrementDtoList = IntStream.range(0, messages.size())
                .mapToObj(idx -> {
                    Message msg = messages.get(idx);
                    String uuid = newUuidBatch.get(idx);
                    CustomChatMemoryDTO dto = repo.msgToDto(msg);
                    dto.setMsgUuid(uuid);
                    return dto;
                })
                .toList();
        sendIncrementRabbitMq(conversationId, incrementDtoList);
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
     * 发送增量消息至RocketMQ，异步入库MySQL（仅新增，不删旧记录）
     */
    private void sendIncrementMq(String conversationId, List<CustomChatMemoryDTO> incrementDtoList) {
        ChatMemoryIncrementMsg mqMsg = new ChatMemoryIncrementMsg();
        mqMsg.setConversationId(conversationId);
        mqMsg.setDtoList(incrementDtoList);
        try {
            rocketMQTemplate.syncSend(CHAT_MEMORY_INCREMENT_TOPIC, mqMsg);
        } catch (Exception e) {
            throw new RuntimeException("发送聊天增量消息MQ失败", e);
        }
    }

    private void sendIncrementRabbitMq(String conversationId, List<CustomChatMemoryDTO> incrementDtoList) {
        ChatMemoryIncrementMsg mqMsg = new ChatMemoryIncrementMsg();
        mqMsg.setConversationId(conversationId);
        mqMsg.setDtoList(incrementDtoList);
        try {
            rabbitTemplate.convertAndSend(CHAT_MEMORY_EXCHANGE, CHAT_MEMORY_ROUTING_KEY, mqMsg);
        } catch (Exception e) {
            throw new RuntimeException("发送聊天增量消息RabbitMQ失败", e);
        }
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
