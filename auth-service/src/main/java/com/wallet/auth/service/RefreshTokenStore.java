package com.wallet.auth.service;

import com.wallet.auth.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "refresh:";
    private static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public String create(String username) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, username, TTL);
        return token;
    }

    public String validateAndGetUsername(String refreshToken) {
        String username = redisTemplate.opsForValue().get(KEY_PREFIX + refreshToken);
        if (username == null) {
            throw new InvalidRefreshTokenException();
        }

        return username;
    }

    public void revoke(String refreshToken) {
        redisTemplate.delete(KEY_PREFIX + refreshToken);
    }

    public String rotate(String oldRefreshToken) {
        String username = validateAndGetUsername(oldRefreshToken);
        revoke(oldRefreshToken);
        return create(username);
    }
}
