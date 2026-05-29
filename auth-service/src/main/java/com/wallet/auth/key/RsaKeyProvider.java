package com.wallet.auth.key;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Component
public class RsaKeyProvider {

    private final RSAKey rsaKey;

    public RsaKeyProvider() {
        this.rsaKey = generateRsaKey();
    }

    private RSAKey generateRsaKey() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    public RSAPublicKey getPublicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract public key", e);
        }
    }

    public RSAPrivateKey getPrivateKey() {
        try {
            return rsaKey.toRSAPrivateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract private key", e);
        }
    }

    public JWKSet getJwkSet() {
        return new JWKSet(rsaKey);
    }
}
