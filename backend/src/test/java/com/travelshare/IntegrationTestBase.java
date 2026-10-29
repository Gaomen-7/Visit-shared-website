package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 后端测试底座：真实启动的应用 + 真实 HTTP + 真 MySQL（独立库 travel_share_test）。
 *
 * <p>不启用测试事务：HTTP 请求在服务端线程执行，测试线程的 {@code @Transactional} 回滚不到它，
 * 那样写只会制造「已回滚」的错觉。这里改为每个测试前清掉注册用户，保留 admin 种子——
 * 效果等价（测试库可反复全量重跑），且不会污染开发库。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void resetRegisteredUsers() {
        jdbc.update("DELETE FROM user WHERE role <> 'ADMIN'");
        // 默认的 HttpURLConnection 在「流式 multipart + 401」时抛 HttpRetryException，换成 Apache HttpClient。
        rest.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    protected HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    protected ResponseEntity<String> postJson(String path, String json, String token) {
        HttpHeaders headers = token == null ? jsonHeaders() : bearerHeaders(token);
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(json, headers), String.class);
    }

    protected ResponseEntity<String> putJson(String path, String json, String token) {
        HttpHeaders headers = token == null ? jsonHeaders() : bearerHeaders(token);
        return rest.exchange(path, HttpMethod.PUT, new HttpEntity<>(json, headers), String.class);
    }

    protected ResponseEntity<String> getWithToken(String path, String token) {
        HttpHeaders headers = token == null ? new HttpHeaders() : bearerHeaders(token);
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    protected JsonNode body(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }

    /** 注册并登录，返回 token；注册失败直接让测试失败。 */
    protected String registerAndLogin(String username, String password) throws Exception {
        ResponseEntity<String> register = postJson("/api/auth/register",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", null);
        assertThat(register.getStatusCodeValue()).isEqualTo(200);

        ResponseEntity<String> login = postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", null);
        assertThat(login.getStatusCodeValue()).isEqualTo(200);
        return body(login).path("data").path("token").asText();
    }

    protected String adminToken() throws Exception {
        ResponseEntity<String> login = postJson("/api/auth/login",
                "{\"username\":\"" + ADMIN_USERNAME + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}", null);
        assertThat(login.getStatusCodeValue()).isEqualTo(200);
        return body(login).path("data").path("token").asText();
    }

    protected long userIdOf(String username) {
        return jdbc.queryForObject("SELECT id FROM user WHERE username = ?", Long.class, username);
    }

    protected void assertErrorEnvelope(ResponseEntity<String> response, int expectedStatus) throws Exception {
        assertThat(response.getStatusCodeValue()).isEqualTo(expectedStatus);
        JsonNode json = body(response);
        assertThat(json.path("code").asInt()).isEqualTo(expectedStatus);
        assertThat(json.path("message").asText()).isNotBlank();
        assertThat(json.path("data").isNull() || json.path("data").isMissingNode()).isTrue();
    }
}
