package com.wj.aisoulmatechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class SessionRedisConfig {
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    public SessionRedisConfig(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
//
//    // 手动创建仓库，传入全局JSON序列化的RedisTemplate
//    @Bean
//    public RedisSessionRepository redisSessionRepository() {
//        return new RedisSessionRepository(redisTemplate);
//    }
//
//    // 手动注册Session过滤器，替代@EnableRedisHttpSession自动装配
//    @Bean
//    public SessionRepositoryFilter<?> sessionRepositoryFilter(RedisSessionRepository redisSessionRepository) {
//        return new SessionRepositoryFilter<>(redisSessionRepository);
//    }

}
