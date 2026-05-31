package com.wallet.auth.service;

import com.wallet.auth.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenStore refreshTokenStore;

    @BeforeEach
    void setUp() {
        refreshTokenStore = new RefreshTokenStore(redisTemplate);
    }

    @Test
    void create_storesTokenAndReturnsIt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = refreshTokenStore.create("dorjan");

        assertThat(token).isNotNull();
        verify(valueOperations).set(eq("refresh:" + token), eq("dorjan"), any(Duration.class));
    }

    @Test
    void validateAndGetUsername_withValidToken_returnsUsername() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh:abc-123")).thenReturn("dorjan");

        String username = refreshTokenStore.validateAndGetUsername("abc-123");

        assertThat(username).isEqualTo("dorjan");
    }

    @Test
    void validateAndGetUsername_withMissingToken_throwsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh:missing")).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenStore.validateAndGetUsername("missing"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void revoke_deletesKey() {
        refreshTokenStore.revoke("abc-123");

        verify(redisTemplate).delete("refresh:abc-123");
    }

    @Test
    void rotate_revokesOldAndCreatesNew() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh:old-token")).thenReturn("dorjan");

        String newToken = refreshTokenStore.rotate("old-token");

        assertThat(newToken)
                .isNotNull()
                .isNotEqualTo("old-token");
        verify(redisTemplate).delete("refresh:old-token");
        verify(valueOperations).set(eq("refresh:" + newToken), eq("dorjan"), any(Duration.class));
    }
}
