package com.wallet.auth.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.wallet.auth.key.RsaKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class JwksController {

    private final RsaKeyProvider rsaKeyProvider;

    @GetMapping("/jwks")
    public Map<String, Object> jwks() {
        JWKSet jwkSet = rsaKeyProvider.getPublicJwkSet();
        return jwkSet.toJSONObject();
    }
}
