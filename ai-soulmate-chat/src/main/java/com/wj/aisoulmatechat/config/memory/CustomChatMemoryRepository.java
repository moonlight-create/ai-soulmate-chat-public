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
    private static final String UUID_KEY_PREFIX = "soulmate:memory:uuid:";
    private final RedisCacheUtil redisCacheUtil;
    private final MyChatMemoryConfigProperties myChatMemoryConfigProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomChatMemoryRepository(RedisCacheUtil redisCacheUtil, MyChatMemoryConfigProperties myChatMemoryConfigProperties) {
        this.redisCacheUtil = redisCacheUtil;
        this.myChatMemoryConfigProperties = myChatMemoryConfigProperties;
    }

    public String getRedisKey(String convId) {
        return redisCacheUtil.buildChatMemoryKey(convId);
    }

    /** Message → DTO 序列化 */
    public CustomChatMemoryDTO msgToDto(Message msg) {
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
    public Message dtoToMsg(CustomChatMemoryDTO dto) {
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

//    @Override
//    public void saveAll(String conversationId, List<Message> messages) {
//        if (conversationId == null || conversationId.isBlank()) {
//            throw new IllegalArgumentException("conversationId不能为空");
//        }
//        if (messages == null) {
//            throw new IllegalArgumentException("messages不能为null");
//        }
//        // 过滤null元素，解决空指针
//        List<Message> validMsgList = messages.stream()
//                .filter(msg -> msg != null)
//                .toList();
//        if (validMsgList.isEmpty()) {
//            return;
//        }
//
//        String key = getRedisKey(conversationId);
//        // 先删key，全量覆盖存储，杜绝消息重复
//        redisCacheUtil.delete(key);
//
//        List<String> jsonArr = new ArrayList<>();
//        try {
//            for (Message m : validMsgList) {
//                String json = objectMapper.writeValueAsString(msgToDto(m));
//                jsonArr.add(json);
//            }
//            redisCacheUtil.listRightPushAll(key, jsonArr);
//            long day = myChatMemoryConfigProperties.getExpireDay();
//            if (day > 0) {
//                redisCacheUtil.expire(key, day, TimeUnit.DAYS);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("消息序列化异常", e);
//        }
//    }

    /**
     * @param conversationId 会话ID
     * @param allMessages 全量消息（历史+新增）
     * @param totalUuidList 和消息列表下标一一对应的完整uuid列表（每条消息都有自己的uuid）
     */
    public void saveAllWithUuid(String conversationId,
                                List<Message> allMessages,
                                List<String> totalUuidList) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId不能为空");
        }
        if (allMessages == null) {
            throw new IllegalArgumentException("messages不能为null");
        }
        List<Message> validMsgList = allMessages.stream()
                .filter(msg -> msg != null)
                .toList();
        if (validMsgList.isEmpty()) {
            return;
        }

        String msgKey = getRedisKey(conversationId);
        String uuidKey = getUuidRedisKey(conversationId);
        // 先清空两个key，全量覆盖
        redisCacheUtil.delete(msgKey);
        redisCacheUtil.delete(uuidKey);

        List<String> jsonArr = new ArrayList<>();
        try {
            for (int i = 0; i < validMsgList.size(); i++) {
                Message m = validMsgList.get(i);
                CustomChatMemoryDTO dto = msgToDto(m);
                // 直接取下标对应的uuid，历史/新增全部赋值，不会为null
                if (i < totalUuidList.size()) {
                    dto.setMsgUuid(totalUuidList.get(i));
                }
                String json = objectMapper.writeValueAsString(dto);
                jsonArr.add(json);
            }
            // 存入消息JSON列表
            redisCacheUtil.listRightPushAll(msgKey, jsonArr);
            // 存入完整uuid列表，和消息一一对应
            redisCacheUtil.listRightPushAll(uuidKey, totalUuidList);

            // 统一过期时间，保证两个key生命周期同步，防止下标错位
            long day = myChatMemoryConfigProperties.getExpireDay();
            if (day > 0) {
                redisCacheUtil.expire(msgKey, day, TimeUnit.DAYS);
                redisCacheUtil.expire(uuidKey, day, TimeUnit.DAYS);
            }
        } catch (Exception e) {
            throw new RuntimeException("消息序列化异常", e);
        }
    }

    // 原有兼容方法
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 纯兼容逻辑，无uuid场景，uuid列表传空
        saveAllWithUuid(conversationId, messages, List.of());
    }

    public String getUuidRedisKey(String conversationId) {
        return UUID_KEY_PREFIX + conversationId;
    }

    // 获取会话全部消息对应的uuid列表
    public List<String> getUuidList(String uuidKey) {
        List<String> uuidList = redisCacheUtil.listGetAll(uuidKey);
        return uuidList == null ? new ArrayList<>() : uuidList;
    }

    /**
     * 不加redis新key执行保存
     * @param conversationId
     * @param allMessages
     * @param totalUuidList
     */
    public void saveAllWithUuidNoExtraKey(String conversationId,
                                          List<Message> allMessages,
                                          List<String> totalUuidList) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId不能为空");
        }
        if (allMessages == null) {
            throw new IllegalArgumentException("messages不能为null");
        }
        List<Message> validMsgList = allMessages.stream()
                .filter(msg -> msg != null)
                .toList();
        if (validMsgList.isEmpty()) {
            return;
        }

        String key = getRedisKey(conversationId);
        redisCacheUtil.delete(key);

        List<String> jsonArr = new ArrayList<>();
        try {
            for (int i = 0; i < validMsgList.size(); i++) {
                Message m = validMsgList.get(i);
                CustomChatMemoryDTO dto = msgToDto(m);

                if (i < totalUuidList.size()) {
                    dto.setMsgUuid(totalUuidList.get(i));
                }
                String json = objectMapper.writeValueAsString(dto);
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

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("conversationId不能为空");
        }
        redisCacheUtil.delete(getRedisKey(conversationId));
    }
}
