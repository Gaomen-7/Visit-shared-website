package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class PasswordApiTest extends IntegrationTestBase {

    @Test
    void wrongOldPasswordIsRejectedWith400() throws Exception {
        String token = registerAndLogin("pwduser1", "123456");
        assertErrorEnvelope(putJson("/api/user/password",
                "{\"oldPassword\":\"wrong\",\"newPassword\":\"newpass1\"}", token), 400);
    }

    @Test
    void newPasswordLengthIsValidated() throws Exception {
        String token = registerAndLogin("pwduser2", "123456");
        assertErrorEnvelope(putJson("/api/user/password",
                "{\"oldPassword\":\"123456\",\"newPassword\":\"12345\"}", token), 400);
        assertErrorEnvelope(putJson("/api/user/password",
                "{\"oldPassword\":\"123456\",\"newPassword\":\"" + String.valueOf('p').repeat(21) + "\"}", token), 400);
    }

    @Test
    void changePasswordSwitchesCredentialsButKeepsIssuedTokenUsable() throws Exception {
        String token = registerAndLogin("pwduser3", "123456");

        ResponseEntity<String> change = putJson("/api/user/password",
                "{\"oldPassword\":\"123456\",\"newPassword\":\"newpass9\"}", token);
        assertThat(change.getStatusCodeValue()).isEqualTo(200);

        ResponseEntity<String> oldLogin = postJson("/api/auth/login",
                "{\"username\":\"pwduser3\",\"password\":\"123456\"}", null);
        assertErrorEnvelope(oldLogin, 400);

        ResponseEntity<String> newLogin = postJson("/api/auth/login",
                "{\"username\":\"pwduser3\",\"password\":\"newpass9\"}", null);
        assertThat(newLogin.getStatusCodeValue()).isEqualTo(200);

        // 定案：改密不强制失效在途 token。
        assertThat(getWithToken("/api/user/profile", token).getStatusCodeValue()).isEqualTo(200);
    }
}
