package com.wallet.auth.service;

import com.wallet.auth.domain.Role;
import com.wallet.auth.domain.User;
import com.wallet.auth.dto.LoginRequest;
import com.wallet.auth.dto.TokenResponse;
import com.wallet.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @InjectMocks
    private LoginService loginService;

    @Test
    void login_withValidCredentials_returnsTokens() {

        LoginRequest request = new LoginRequest("dorjan", "Test1234");
        User user = new User("dorjan", "hashedPassword", Set.of(Role.USER));

        when(userRepository.findByUsername("dorjan")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test1234", "hashedPassword")).thenReturn(true);
        when(tokenService.generateToken(eq("dorjan"), anySet())).thenReturn("access-token");
        when(refreshTokenStore.create("dorjan")).thenReturn("refresh-token");

        TokenResponse response = loginService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
    }

    @Test
    void login_withWrongPassword_throwsBadCredentials() {

        LoginRequest request = new LoginRequest("dorjan", "WrongPass1");
        User user = new User("dorjan", "hashedPassword", Set.of(Role.USER));

        when(userRepository.findByUsername("dorjan")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(tokenService, never()).generateToken(any(), any());
        verify(refreshTokenStore, never()).create(any());
    }

    @Test
    void login_withNonExistentUser_throwsBadCredentials() {

        LoginRequest request = new LoginRequest("ghost", "Test1234");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(passwordEncoder, never()).matches(any(), any());
    }
}
