package com.stofina.app.userservice.service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${redis.service.prefix}")
    private String prefix;

    @Value("${redis.service.ttl.refresh}")
    private long refreshTokenTtl;

    @Autowired
    public RefreshTokenRedisService(@Qualifier("stofinaRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();

        String tokenKey = buildTokenKey(token);
        String userKey = buildUserKey(userId);

        redisTemplate.opsForValue().set(tokenKey, userId.toString(), Duration.ofSeconds(refreshTokenTtl));
        redisTemplate.opsForValue().set(userKey, token, Duration.ofSeconds(refreshTokenTtl));

        return token;
    }


    public Optional<Long> getUserIdFromRefreshToken(String token) {
        String tokenKey = buildTokenKey(token);
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);
        return Optional.ofNullable(userIdStr).map(Long::parseLong);
    }

    public boolean deleteRefreshTokenByUserId(Long userId) {
        String userKey = buildUserKey(userId);
        String token = redisTemplate.opsForValue().get(userKey);

        if (token != null) {
            String tokenKey = buildTokenKey(token);
            redisTemplate.delete(userKey);
            redisTemplate.delete(tokenKey);
            return true;
        }

        return false;
    }

    private String buildTokenKey(String token) {
        return prefix + "refresh:token:" + token;
    }

    private String buildUserKey(Long userId) {
        return prefix + "refresh:user:" + userId;
    }
}
