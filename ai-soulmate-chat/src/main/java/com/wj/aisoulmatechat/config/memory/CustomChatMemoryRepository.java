package com.wj.aisoulmatechat.config.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wj.aisoulmatechat.config.properties.MyChatMemoryConfigProperties;
import com.wj.aisoulmatechat.util.RedisCacheUtil;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class CustomChatMemoryRepository implements ChatMemoryRepository {
    private static final String MEM_KEY_PREFIX = "soulmate:memory:";
    private final RedisCacheUtil redisCacheUtil;
    private final MyChatMemoryConfigProperties myChatMemoryConfigProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomChatMemoryRepository(RedisCacheUtil redisCacheUtil, MyChatMemoryConfigProperties myChatMemoryConfigProperties) {
        this.redisCacheUtil = redisCacheUtil;
        this.myChatMemoryConfigProperties = myChatMemoryConfigProperties;
    }

    private String getRedisKey(String convId) {
        return redisCacheUtil.buildChatMemoryKey(convId);
    }

    /** Message → DTO 序列化(补齐四种消息独有字段，对标课程) */
    private CustomChatMemoryDTO msgToDto(Message msg) {
        CustomChatMemoryDTO dto = new CustomChatMemoryDTO();
        dto.setContent(msg.getText());
        dto.setMetadata(msg.getMetadata());

        if (msg instanceof SystemMessage) {
            dto.setType(0);
        } else if (msg instanceof UserMessage userMsg) {
            dto.setType(1);
            dto.setMedia(userMsg.getMedia());
        } else if (msg instanceof AssistantMessage assistMsg) {
            dto.setType(2);
            dto.setToolCalls(assistMsg.getToolCalls());
        } else if (msg instanceof ToolResponseMessage toolMsg) {
            dto.setType(3);
            dto.setToolResponses(toolMsg.getResponses());
        }
        return dto;
    }

    /** DTO → Message 反序列化，四种类型手动new，规避Jackson反射报错 */
    private Message dtoToMsg(CustomChatMemoryDTO dto) {
        return switch (dto.getType()) {
            case 0 -> new SystemMessage(dto.getContent());
            case 1 -> UserMessage.builder()
                    .text(dto.getContent())
                    .media(dto.getMedia())
                    .metadata(dto.getMetadata())
                    .build();
            case 2 -> new AssistantMessage(dto.getContent(), dto.getProperties(), dto.getToolCalls());
            case 3 -> new ToolResponseMessage(dto.getToolResponses(), dto.getMetadata());
            default -> throw new RuntimeException("未知消息类型:" + dto.getType());
        };
    }

    @Override
    public List<String> findConversationIds() {
        Set<String> keys = redisCacheUtil.keys(MEM_KEY_PREFIX + "*");
        return keys.stream().map(k -> k.replace(MEM_KEY_PREFIX, "")).toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId不能为空");
        }
        String key = getRedisKey(conversationId);
        List<String> jsonList = redisCacheUtil.range(key, 0, -1);
        if (jsonList.isEmpty()) return new ArrayList<>();

        List<Message> msgs = new ArrayList<>();
        try {
            for (String json : jsonList) {
                CustomChatMemoryDTO dto = objectMapper.readValue(json, CustomChatMemoryDTO.class);
                msgs.add(dtoToMsg(dto));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgs;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 官方规范参数校验
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId不能为空");
        }
        if (messages == null) {
            throw new IllegalArgumentException("messages不能为null");
        }
        // 过滤null元素，解决ImmutableList.contains空指针
        List<Message> validMsgList = messages.stream()
                .filter(msg -> msg != null)
                .toList();
        if (validMsgList.isEmpty()) {
            return;
        }

        String key = getRedisKey(conversationId);
        // Repository标准：先删key，全量覆盖存储，杜绝消息重复
        redisCacheUtil.delete(key);

        List<String> jsonArr = new ArrayList<>();
        try {
            for (Message m : validMsgList) {
                String json = objectMapper.writeValueAsString(msgToDto(m));
                jsonArr.add(json);
            }
            redisCacheUtil.listRightPushAll(key, jsonArr);
            long day = myChatMemoryConfigProperties.getExpireDay();
            if (day > 0) {
                redisCacheUtil.expire(key, day, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            throw new RuntimeException("消息序列化异常", e);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId不能为空");
        }
        redisCacheUtil.delete(getRedisKey(conversationId));
    }
}
