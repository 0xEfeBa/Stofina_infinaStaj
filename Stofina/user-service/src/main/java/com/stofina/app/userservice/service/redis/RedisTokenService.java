package com.stofina.app.userservice.service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${redis.service.prefix}")
    private String prefix;

    @Value("${redis.service.ttl.activation}")
    private long activationTtl;

    @Value("${redis.service.ttl.reset}")
    private long resetTtl;

    @Autowired
    public RedisTokenService(@Qualifier("stofinaRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String type, String token, String email) {
        Duration ttl = getTTLForType(type);
        redisTemplate.opsForValue().set(buildKey(type, token), email, ttl);
    }

    public Optional<String> getEmail(String type, String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(type, token)));
    }

    public void deleteToken(String type, String token) {
        redisTemplate.delete(buildKey(type, token));
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    private String buildKey(String type, String token) {
        return prefix + type + ":" + token;
    }

    private Duration getTTLForType(String type) {
        return switch (type) {
            case "activation" -> Duration.ofSeconds(activationTtl);
            case "reset" -> Duration.ofSeconds(resetTtl);
            default -> Duration.ofHours(1);
        };
    }
}
