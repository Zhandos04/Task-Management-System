package org.example.taskmanagementsystem.service.impl;

import org.example.taskmanagementsystem.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    // Используем ConcurrentHashMap для хранения токенов в памяти
    private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    /**
     * Добавить токен в черный список.
     * @param token Токен, который нужно занести в черный список.
     * @param expirationTime Время истечения токена.
     */
    @Override
    public void addTokenToBlacklist(String token, Date expirationTime) {
        long timeToLive = expirationTime.getTime() - System.currentTimeMillis();
        if (timeToLive > 0) {
            tokenBlacklist.put(token, expirationTime.getTime());
        }
    }

    /**
     * Проверить, находится ли токен в черном списке.
     * @param token Токен, который нужно проверить.
     * @return True, если токен находится в черном списке.
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = tokenBlacklist.get(token);
        // Если токен найден в черном списке и еще не истек его срок
        return expirationTime != null && expirationTime > System.currentTimeMillis();
    }
}
