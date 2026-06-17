package com.wj.aisoulmatechat.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wj.aisoulmatechat.config.memory.CustomChatMemoryDTO;
import com.wj.aisoulmatechat.entity.ChatMemoryEntity;
import com.wj.aisoulmatechat.mapper.ChatMemoryMapper;
import com.wj.aisoulmatechat.service.ChatMemoryDbService;
import com.wj.aisoulmatechat.vo.ChatMemoryGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMemoryDbServiceImpl extends ServiceImpl<ChatMemoryMapper, ChatMemoryEntity> implements ChatMemoryDbService {
    private final ChatMemoryMapper chatMemoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertIncrement(String convId, List<CustomChatMemoryDTO> dtoList) {
        List<ChatMemoryEntity> entityList = dtoList.stream().map(dto -> {
            ChatMemoryEntity entity = new ChatMemoryEntity();
            entity.setMsgUuid(dto.getMsgUuid());
            entity.setConversationId(convId);
            entity.setMsgType(dto.getType());
            entity.setContent(dto.getContent());
            try {
                entity.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
                entity.setMedia(objectMapper.writeValueAsString(dto.getMedia()));
                entity.setToolCalls(objectMapper.writeValueAsString(dto.getToolCalls()));
                entity.setToolResponses(objectMapper.writeValueAsString(dto.getToolResponses()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("消息元数据序列化失败", e);
            }
            return entity;
        }).toList();

        this.saveBatch(entityList, 20);

//        chatMemoryMapper.batchInsertIgnore(entityList);
    }

    @Override
    public List<ChatMemoryGroupVO> getConversationGroupByDay(String conversationId) {
        // 查询聊天记录
        List<ChatMemoryEntity> allList = baseMapper.listAllByConversation(conversationId);
        if (CollUtil.isEmpty(allList)) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        // 按日期字符串分组
        Map<String, List<ChatMemoryEntity>> groupMap = allList.stream()
                .collect(Collectors.groupingBy(item -> item.getCreateTime().toLocalDate().toString()));

        List<ChatMemoryGroupVO> result = new ArrayList<>();
        for (Map.Entry<String, List<ChatMemoryEntity>> entry : groupMap.entrySet()) {
            String dateStr = entry.getKey();
            List<ChatMemoryEntity> msgList = entry.getValue();
            ChatMemoryGroupVO vo = new ChatMemoryGroupVO();
            LocalDate date = LocalDate.parse(dateStr);
            if (date.isEqual(today)) {
                vo.setGroupTitle("今天");
            } else if (date.isEqual(yesterday)) {
                vo.setGroupTitle("昨天");
            } else {
                vo.setGroupTitle(dateStr);
            }
            vo.setMsgList(msgList);
            result.add(vo);
        }

        // 自定义排序：今天 > 昨天 > 日期倒序
        result.sort((a, b) -> {
            String t1 = a.getGroupTitle();
            String t2 = b.getGroupTitle();
            int w1 = getWeight(t1);
            int w2 = getWeight(t2);
            if (w1 != w2) {
                return Integer.compare(w2, w1);
            }
            return b.getGroupTitle().compareTo(a.getGroupTitle());
        });

        return result;
    }

    private int getWeight(String title) {
        return switch (title) {
            case "今天" -> 3;
            case "昨天" -> 2;
            default -> 1;
        };
    }

}
