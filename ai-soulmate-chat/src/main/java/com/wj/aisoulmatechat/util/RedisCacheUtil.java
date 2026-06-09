package com.wj.aisoulmatechat.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {
    private static final String REDIS_KEY_PREFIX = "soulmate:system:user:";
    private static final String MEM_KEY_PREFIX = "soulmate:memory:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long day) {
        stringRedisTemplate.opsForValue().set(key, value, day, TimeUnit.DAYS);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public Set<String> keys(String key) {
         return stringRedisTemplate.keys(key);
    }

    public List<String> range(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    public Long listRightPushAll(String key, List<String> dataList) {
        return stringRedisTemplate.opsForList().rightPushAll(key, dataList);
    }

    public Long increment(String counterKey) {
        return stringRedisTemplate.opsForValue().increment(counterKey);
    }

    public String buildSystemPromptKey(Long userId, Long soulmateId) {
        return REDIS_KEY_PREFIX + userId + ":soulmate:" + soulmateId;
    }

    public String buildChatMemoryKey(String convId) {
        return MEM_KEY_PREFIX + convId;
    }

}
