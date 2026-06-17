package com.wj.aisoulmatechat.mq;

import com.wj.aisoulmatechat.config.memory.CustomChatMemoryDTO;
import com.wj.aisoulmatechat.config.memory.CustomFullWindowChatMemory;
import com.wj.aisoulmatechat.entity.ChatMemoryIncrementMsg;
import com.wj.aisoulmatechat.service.ChatMemoryDbService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = CustomFullWindowChatMemory.CHAT_MEMORY_INCREMENT_TOPIC,
        consumerGroup = "chat_memory_increment_consumer_group"
)
public class ChatMemoryIncrementRocketConsumer implements RocketMQListener<ChatMemoryIncrementMsg> {
    private final ChatMemoryDbService chatMemoryDbService;

    @Override
    public void onMessage(ChatMemoryIncrementMsg mqMsg) {
        String convId = mqMsg.getConversationId();
        List<CustomChatMemoryDTO> dtoList = mqMsg.getDtoList();
        // 批量增量插入数据库，自带幂等
        chatMemoryDbService.batchInsertIncrement(convId, dtoList);
    }
}
