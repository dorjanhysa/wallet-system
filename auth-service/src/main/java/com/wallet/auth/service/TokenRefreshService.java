package com.wallet.auth.service;

import com.wallet.auth.domain.User;
import com.wallet.auth.dto.TokenResponse;
import com.wallet.auth.exception.InvalidRefreshTokenException;
import com.wallet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final RefreshTokenStore refreshTokenStore;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public TokenResponse refresh(String oldRefreshToken) {
        String username = refreshTokenStore.validateAndGetUsername(oldRefreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidRefreshTokenException::new);

        String newRefreshToken = refreshTokenStore.rotate(oldRefreshToken);
        String accessToken = tokenService.generateToken(username, user.getRoles());

        return new TokenResponse(accessToken, newRefreshToken, "Bearer", 900);
    }
}
