package com.wj.aisoulmatechat.config.mq;

import com.wj.aisoulmatechat.config.memory.CustomFullWindowChatMemory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String CHAT_MEMORY_QUEUE = "chat_memory_increment_queue";
    public static final String CHAT_MEMORY_EXCHANGE = "chat_memory_increment_exchange";
    public static final String ROUTING_KEY = "chat.memory.increment";

    @Bean
    public DirectExchange chatMemoryExchange() {
        return ExchangeBuilder.directExchange(CHAT_MEMORY_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue chatMemoryQueue() {
        return QueueBuilder.durable(CHAT_MEMORY_QUEUE).build();
    }

    @Bean
    public Binding chatMemoryBinding(Queue chatMemoryQueue, DirectExchange chatMemoryExchange) {
        return BindingBuilder.bind(chatMemoryQueue)
                .to(chatMemoryExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
