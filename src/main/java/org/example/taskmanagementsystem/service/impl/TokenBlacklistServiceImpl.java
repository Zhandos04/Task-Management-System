package org.example.taskmanagementsystem.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.service.TokenBlacklistService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addTokenToBlacklist(String token, Date expirationTime) {
        long timeToLiveMillis = expirationTime.getTime() - System.currentTimeMillis();
        if (timeToLiveMillis > 0) {
            long ttlSeconds = timeToLiveMillis / 1000;
            redisTemplate.opsForValue().set(token, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
        }
    }
    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

    @PostConstruct
    public void init() {
        System.out.println("Redis Host: " + redisTemplate.getConnectionFactory().getConnection().ping());
    }

}
