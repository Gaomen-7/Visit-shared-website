package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 接缝 A · 注册：真实 HTTP → 拦截器 → Controller → Service → Mapper → MySQL。
 */
class RegisterApiTest extends IntegrationTestBase {

    @Test
    void registerSuccessReturnsMinimalViewAndPersistsBcryptPassword() throws Exception {
        ResponseEntity<String> response = postJson("/api/auth/register",
                "{\"username\":\"alice\",\"password\":\"secret123\"}", null);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        JsonNode data = body(response).path("data");
        assertThat(data.path("id").asLong()).isPositive();
        assertThat(data.path("username").asText()).isEqualTo("alice");
        assertThat(data.path("nickname").asText()).isEqualTo("alice");

        String stored = jdbc.queryForObject(
                "SELECT password FROM user WHERE username = 'alice'", String.class);
        assertThat(stored).startsWith("$2");
        assertThat(stored).isNotEqualTo("secret123");
        assertThat(jdbc.queryForObject("SELECT role FROM user WHERE username = 'alice'", String.class))
                .isEqualTo("USER");
    }

    @Test
    void usernameBoundaries() throws Exception {
        registerExpectStatus("abc", "123456", 200);
        registerExpectStatus(repeat('a', 3), "123456", 200);
        registerExpectStatus(repeat('b', 20), "123456", 200);
        registerExpectStatus("ab", "123456", 400);
        registerExpectStatus(repeat('c', 21), "123456", 400);
    }

    @Test
    void passwordBoundaries() throws Exception {
        registerExpectStatus("pwuser1", "123456", 200);
        registerExpectStatus("pwuser2", repeat('x', 20), 200);
        registerExpectStatus("pwuser3", "12345", 400);
        registerExpectStatus("pwuser4", repeat('y', 21), 400);
    }

    @Test
    void usernameWithIllegalCharacterIsRejected() throws Exception {
        registerExpectStatus("ab-c", "123456", 400);
    }

    @Test
    void duplicateUsernameIsBusinessErrorIncludingCaseVariant() throws Exception {
        registerExpectStatus("Tom", "123456", 200);
        ResponseEntity<String> duplicate = postJson("/api/auth/register",
                "{\"username\":\"tom\",\"password\":\"123456\"}", null);

        assertErrorEnvelope(duplicate, 400);
        assertThat(body(duplicate).path("message").asText()).contains("用户名已存在");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user WHERE username = 'Tom'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void roleParameterIsIgnored() throws Exception {
        ResponseEntity<String> response = postJson("/api/auth/register",
                "{\"username\":\"roleuser\",\"password\":\"123456\",\"role\":\"ADMIN\"}", null);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT role FROM user WHERE username = 'roleuser'", String.class))
                .isEqualTo("USER");
    }

    @Test
    void missingPasswordIsEnvelope400() throws Exception {
        assertErrorEnvelope(postJson("/api/auth/register", "{\"username\":\"nopw\"}", null), 400);
    }

    private void registerExpectStatus(String username, String password, int expected) throws Exception {
        ResponseEntity<String> response = postJson("/api/auth/register",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", null);
        if (expected == 200) {
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
        } else {
            assertErrorEnvelope(response, expected);
        }
    }

    private String repeat(char c, int count) {
        return String.valueOf(c).repeat(count);
    }
}
