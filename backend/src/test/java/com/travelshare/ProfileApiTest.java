package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

class ProfileApiTest extends IntegrationTestBase {

    @Test
    void profileReturnsDocumentedFieldSet() throws Exception {
        String token = registerAndLogin("profileuser", "123456");

        ResponseEntity<String> response = getWithToken("/api/user/profile", token);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        JsonNode data = body(response).path("data");
        assertThat(data.path("id").asLong()).isEqualTo(userIdOf("profileuser"));
        assertThat(data.path("username").asText()).isEqualTo("profileuser");
        assertThat(data.path("nickname").asText()).isEqualTo("profileuser");
        assertThat(data.path("avatar").isNull()).isTrue();
        assertThat(data.path("intro").isNull()).isTrue();
        assertThat(data.path("createTime").asText()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void updateProfileTakesEffectImmediately() throws Exception {
        String token = registerAndLogin("edituser", "123456");

        ResponseEntity<String> response = putJson("/api/user/profile",
                "{\"nickname\":\"旅行者小王\",\"avatar\":\"/uploads/x.png\",\"intro\":\"爱旅行\"}", token);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        JsonNode data = body(response).path("data");
        assertThat(data.path("nickname").asText()).isEqualTo("旅行者小王");
        assertThat(data.path("avatar").asText()).isEqualTo("/uploads/x.png");
        assertThat(data.path("intro").asText()).isEqualTo("爱旅行");

        JsonNode reread = body(getWithToken("/api/user/profile", token)).path("data");
        assertThat(reread.path("nickname").asText()).isEqualTo("旅行者小王");
    }

    @Test
    void overlongNicknameAndIntroAreRejectedWith400() throws Exception {
        String token = registerAndLogin("longuser", "123456");

        assertErrorEnvelope(putJson("/api/user/profile",
                "{\"nickname\":\"" + String.valueOf('n').repeat(21) + "\"}", token), 400);
        assertErrorEnvelope(putJson("/api/user/profile",
                "{\"nickname\":\"ok\",\"intro\":\"" + String.valueOf('i').repeat(201) + "\"}", token), 400);
    }

    @Test
    void blankNicknameIsRejectedWith400() throws Exception {
        String token = registerAndLogin("blankuser", "123456");
        assertErrorEnvelope(putJson("/api/user/profile", "{\"nickname\":\"   \"}", token), 400);
    }

    @Test
    void usernameInBodyHasNoEffect() throws Exception {
        String token = registerAndLogin("fixedname", "123456");

        ResponseEntity<String> response = putJson("/api/user/profile",
                "{\"nickname\":\"新昵称\",\"username\":\"hacked\"}", token);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(body(response).path("data").path("username").asText()).isEqualTo("fixedname");
        assertThat(jdbc.queryForObject("SELECT username FROM user WHERE id = ?", String.class,
                userIdOf("fixedname"))).isEqualTo("fixedname");
    }
}
