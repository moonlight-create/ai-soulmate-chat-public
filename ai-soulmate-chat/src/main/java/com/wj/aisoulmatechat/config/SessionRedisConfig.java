package com.wj.aisoulmatechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.SessionRepositoryFilter;

//@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class SessionRedisConfig {
    private final RedisTemplate<String, Object> redisTemplate;

    public SessionRedisConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public RedisSessionRepository redisSessionRepository() {
        return new RedisSessionRepository(redisTemplate);
    }

    @Bean
    public SessionRepositoryFilter<?> sessionRepositoryFilter(RedisSessionRepository redisSessionRepository) {
        return new SessionRepositoryFilter<>(redisSessionRepository);
    }

}
