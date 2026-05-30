package com.wallet.auth.service;

import com.wallet.auth.domain.Role;
import com.wallet.auth.domain.User;
import com.wallet.auth.dto.RegisterRequest;
import com.wallet.auth.exception.UsernameAlreadyExistsException;
import com.wallet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed: username already exists: {}", request.username());
            throw new UsernameAlreadyExistsException(request.username());
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.username(), hashedPassword, Set.of(Role.USER));

        userRepository.save(user);
        log.info("User registered successfully: {}", request.username());
    }
}
