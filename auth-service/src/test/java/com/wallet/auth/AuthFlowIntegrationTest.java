package com.wallet.auth;

import com.wallet.auth.dto.LoginRequest;
import com.wallet.auth.dto.RefreshRequest;
import com.wallet.auth.dto.RegisterRequest;
import com.wallet.auth.dto.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class AuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullAuthFlow_register_login_refresh_rotates() {

        RegisterRequest registerRequest = new RegisterRequest("alice", "Secret123");
        ResponseEntity<Void> registerResponse =
                restTemplate.postForEntity("/api/auth/register", registerRequest, Void.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        LoginRequest loginRequest = new LoginRequest("alice", "Secret123");
        ResponseEntity<TokenResponse> loginResponse =
                restTemplate.postForEntity("/api/auth/login", loginRequest, TokenResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        TokenResponse tokens = loginResponse.getBody();
        assertThat(tokens).isNotNull();
        Assertions.assertNotNull(tokens);
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        String firstRefresh = tokens.refreshToken();

        RefreshRequest refreshRequest = new RefreshRequest(firstRefresh);
        ResponseEntity<TokenResponse> refreshResponse =
                restTemplate.postForEntity("/api/auth/refresh", refreshRequest, TokenResponse.class);
        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        TokenResponse newTokens = refreshResponse.getBody();
        assertThat(newTokens).isNotNull();
        Assertions.assertNotNull(newTokens);
        assertThat(newTokens.refreshToken()).isNotEqualTo(firstRefresh);

        ResponseEntity<String> reusedOldToken =
                restTemplate.postForEntity("/api/auth/refresh", refreshRequest, String.class);
        assertThat(reusedOldToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
