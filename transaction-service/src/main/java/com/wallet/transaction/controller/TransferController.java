package com.wallet.transaction.controller;

import com.wallet.transaction.dto.CreateTransferRequest;
import com.wallet.transaction.dto.TransferResponse;
import com.wallet.transaction.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> initiateTransfer(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTransferRequest request) {

        TransferResponse response = transferService.initiateTransfer(jwt.getSubject(), request);

        URI location = URI.create("/api/transfers/" + response.id());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public TransferResponse getTransfer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return transferService.getTransfer(id, jwt.getSubject());
    }
}
