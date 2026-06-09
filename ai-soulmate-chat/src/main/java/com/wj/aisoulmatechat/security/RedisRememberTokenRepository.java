package com.wj.aisoulmatechat.security;

import com.wj.aisoulmatechat.dto.RememberTokenDTO;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisRememberTokenRepository implements PersistentTokenRepository {
    private static final String TOKEN_KEY_PREFIX = "REMEMBER_TOKEN:";
    private static final String USER_INDEX_PREFIX = "USER_TOKEN_MAP:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final long expireSeconds;

    public RedisRememberTokenRepository(RedisConnectionFactory factory, long expireSeconds) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer strSer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(strSer);
        template.setValueSerializer(jsonSer);
        template.setHashKeySerializer(strSer);
        template.setHashValueSerializer(jsonSer);
        template.afterPropertiesSet();
        this.redisTemplate = template;
        this.expireSeconds = expireSeconds;
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        String tokenKey = TOKEN_KEY_PREFIX + token.getSeries();
        String userIndexKey = USER_INDEX_PREFIX + token.getUsername();
        RememberTokenDTO dto = new RememberTokenDTO(token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
        redisTemplate.opsForValue().set(tokenKey, dto, expireSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForHash().put(userIndexKey, token.getSeries(), "");
        redisTemplate.expire(userIndexKey, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        String key = TOKEN_KEY_PREFIX + seriesId;
        RememberTokenDTO dto = (RememberTokenDTO) redisTemplate.opsForValue().get(key);
        if(dto == null) return null;
        return new PersistentRememberMeToken(dto.getUsername(), dto.getSeries(), dto.getTokenValue(), dto.getLastUsed());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        PersistentRememberMeToken oldToken = getTokenForSeries(series);
        if (oldToken == null) return;
        String tokenKey = TOKEN_KEY_PREFIX + series;
        redisTemplate.delete(tokenKey);
        PersistentRememberMeToken newToken = new PersistentRememberMeToken(oldToken.getUsername(), series, tokenValue, lastUsed);
        RememberTokenDTO dto = new RememberTokenDTO(newToken.getUsername(), newToken.getSeries(), newToken.getTokenValue(), newToken.getDate());
        redisTemplate.opsForValue().set(tokenKey, dto, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void removeUserTokens(String username) {
        String userIndexKey = USER_INDEX_PREFIX + username;
        Set<Object> seriesSet = redisTemplate.opsForHash().keys(userIndexKey);
        if (seriesSet == null || seriesSet.isEmpty()) return;
        for (Object series : seriesSet) {
            String tokenKey = TOKEN_KEY_PREFIX + series;
            redisTemplate.delete(tokenKey);
        }
        redisTemplate.delete(userIndexKey);
    }
}
