package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.travelshare.common.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class LoginApiTest extends IntegrationTestBase {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void loginSuccessReturnsTokenAndRole() throws Exception {
        registerAndLogin("bob", "secret123");

        ResponseEntity<String> response = postJson("/api/auth/login",
                "{\"username\":\"bob\",\"password\":\"secret123\"}", null);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        JsonNode data = body(response).path("data");
        assertThat(data.path("token").asText()).isNotBlank();
        assertThat(data.path("userId").asLong()).isEqualTo(userIdOf("bob"));
        assertThat(data.path("username").asText()).isEqualTo("bob");
        assertThat(data.path("nickname").asText()).isEqualTo("bob");
        assertThat(data.path("role").asText()).isEqualTo("USER");
        assertThat(data.path("avatar").isNull()).isTrue();
    }

    @Test
    void tokenExpiresInTwoHours() throws Exception {
        String token = registerAndLogin("expiryuser", "secret123");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        long seconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertThat(seconds).isEqualTo(2 * 3600);
    }

    @Test
    void wrongPasswordAndUnknownUserShareTheSameMessage() throws Exception {
        registerAndLogin("carol", "secret123");

        ResponseEntity<String> wrongPassword = postJson("/api/auth/login",
                "{\"username\":\"carol\",\"password\":\"wrong-pass\"}", null);
        ResponseEntity<String> unknownUser = postJson("/api/auth/login",
                "{\"username\":\"nobody\",\"password\":\"whatever\"}", null);

        assertErrorEnvelope(wrongPassword, 400);
        assertErrorEnvelope(unknownUser, 400);
        assertThat(body(wrongPassword).path("message").asText())
                .isEqualTo(body(unknownUser).path("message").asText());
    }

    @Test
    void disabledAccountIsRejectedWithDistinctMessage() throws Exception {
        registerAndLogin("dave", "secret123");
        jdbc.update("UPDATE user SET status = 0 WHERE username = 'dave'");

        ResponseEntity<String> response = postJson("/api/auth/login",
                "{\"username\":\"dave\",\"password\":\"secret123\"}", null);
        assertErrorEnvelope(response, 400);
        assertThat(body(response).path("message").asText()).contains("禁用");
    }

    @Test
    void adminSeedAccountCanLogin() throws Exception {
        ResponseEntity<String> response = postJson("/api/auth/login",
                "{\"username\":\"" + ADMIN_USERNAME + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}", null);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(body(response).path("data").path("role").asText()).isEqualTo("ADMIN");
    }
}
