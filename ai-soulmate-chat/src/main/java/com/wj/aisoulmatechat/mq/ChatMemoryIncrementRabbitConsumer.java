package com.wj.aisoulmatechat.mq;

import com.wj.aisoulmatechat.config.memory.CustomChatMemoryDTO;
import com.wj.aisoulmatechat.config.mq.RabbitMqConfig;
import com.wj.aisoulmatechat.entity.mq.ChatMemoryIncrementMsg;
import com.wj.aisoulmatechat.service.ChatMemoryDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMemoryIncrementRabbitConsumer {
    private final ChatMemoryDbService chatMemoryDbService;

    @RabbitListener(queues = RabbitMqConfig.CHAT_MEMORY_QUEUE)
    public void consume(ChatMemoryIncrementMsg mqMsg) {
        String convId = mqMsg.getConversationId();
        List<CustomChatMemoryDTO> dtoList = mqMsg.getDtoList();
        chatMemoryDbService.batchInsertIncrement(convId, dtoList);
    }
}
