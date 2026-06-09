package com.wallet.account.controller;

import com.wallet.account.dto.AccountResponse;
import com.wallet.account.dto.AmountRequest;
import com.wallet.account.dto.CreateAccountRequest;
import com.wallet.account.dto.TransactionResponse;
import com.wallet.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> getMyAccounts(@AuthenticationPrincipal Jwt jwt) {
        return accountService.getMyAccounts(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return accountService.getAccount(id, jwt.getSubject());
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateAccountRequest request
    ) {

        AccountResponse accountCreated = accountService.createAccount(jwt.getSubject(), request);
        URI location = URI.create("/api/accounts/" + accountCreated.id());
        return ResponseEntity.created(location).body(accountCreated);
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody AmountRequest request)
    {
        return accountService.deposit(id, jwt.getSubject(), request.amount());
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            @Valid @RequestBody AmountRequest request)
    {
        return accountService.withdraw(id, jwt.getSubject(), request.amount());
    }

    @GetMapping("/{id}/transactions")
    public Page<TransactionResponse> getMyTransactions(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
            Pageable pageable) {
        return accountService.getMyTransactions(id, jwt.getSubject(), pageable);
    }
}
