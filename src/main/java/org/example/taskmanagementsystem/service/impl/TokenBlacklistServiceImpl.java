package org.example.taskmanagementsystem.service.impl;

import org.example.taskmanagementsystem.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    @Override
    public void addTokenToBlacklist(String token, Date expirationTime) {
        long timeToLive = expirationTime.getTime() - System.currentTimeMillis();
        if (timeToLive > 0) {
            tokenBlacklist.put(token, expirationTime.getTime());
        }
    }
    @Override
    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = tokenBlacklist.get(token);
        return expirationTime != null && expirationTime > System.currentTimeMillis();
    }

}
