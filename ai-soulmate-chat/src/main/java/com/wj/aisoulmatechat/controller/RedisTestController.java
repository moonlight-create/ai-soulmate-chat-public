package com.wj.aisoulmatechat.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

@RestController
public class RedisTestController {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis/test")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String testRedis(){
        redisTemplate.opsForValue().set("test:demo","自定义Template数据");
        return redisTemplate.opsForValue().get("test:demo")+"";
    }

    @GetMapping("/redis/str")
    public String testStr(){
        stringRedisTemplate.opsForValue().set("test:str","原生字符串");
        return stringRedisTemplate.opsForValue().get("test:str");
    }

}
