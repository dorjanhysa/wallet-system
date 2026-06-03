package com.wallet.account.controller;

import com.wallet.account.dto.AccountResponse;
import com.wallet.account.dto.CreateAccountRequest;
import com.wallet.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public AccountResponse getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        return accountService.getMyAccount(username);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAccountRequest request
    ) {
        String username = jwt.getSubject();
        AccountResponse accountCreated = accountService.createAccount(username, request);

        URI location = URI.create("/api/accounts/" + accountCreated.id());
        return ResponseEntity.created(location).body(accountCreated);
    }
}
