package com.wallet.auth.controller;

import com.wallet.auth.dto.LoginRequest;
import com.wallet.auth.dto.RegisterRequest;
import com.wallet.auth.dto.TokenResponse;
import com.wallet.auth.service.LoginService;
import com.wallet.auth.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final RegistrationService registrationService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registrationService.register(request);
    }
}
