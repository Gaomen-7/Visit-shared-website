package com.travelshare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.travelshare.common.JwtUtil;
import com.travelshare.common.JwtInterceptor;
import com.travelshare.common.LoginUserContext;

class AuthorizationApiTest extends IntegrationTestBase {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Test
    void publicPathAllowsAnonymous() throws Exception {
        assertThat(getWithToken("/api/ping", null).getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void loginPathRejectsAnonymousWith401() throws Exception {
        assertErrorEnvelope(getWithToken("/api/user/profile", null), 401);
    }

    @Test
    void adminPathRejectsUserWith403AndAllowsAdminWith200() throws Exception {
        String userToken = registerAndLogin("normaluser", "123456");

        ResponseEntity<String> forbidden = getWithToken("/api/admin/ping", userToken);
        assertErrorEnvelope(forbidden, 403);

        ResponseEntity<String> allowed = getWithToken("/api/admin/ping", adminToken());
        assertThat(allowed.getStatusCodeValue()).isEqualTo(200);
        assertThat(body(allowed).path("data").path("role").asText()).isEqualTo("ADMIN");
    }

    @Test
    void tamperedSignatureIsRejectedWith401() throws Exception {
        String token = registerAndLogin("tamperuser", "123456");
        assertErrorEnvelope(getWithToken("/api/user/profile", token + "x"), 401);
    }

    @Test
    void expiredTokenIsRejectedWith401() throws Exception {
        registerAndLogin("expireduser", "123456");
        String expired = jwtUtil.generateToken(userIdOf("expireduser"), "USER", -60_000L);
        assertErrorEnvelope(getWithToken("/api/user/profile", expired), 401);
    }

    /**
     * 本模块唯一一处白盒测试：ThreadLocal 清理无法用纯 HTTP 稳定复现（HTTP 客户端看不到线程池状态），
     * 只能直接驱动拦截器 afterCompletion 后断言上下文已空。例外理由已登记在 spec 的测试决策中。
     */
    @Test
    void threadLocalContextIsClearedAfterRequestEvenOnException() throws Exception {
        String token = registerAndLogin("ctxuser", "123456");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(jwtInterceptor.preHandle(request, response, new Object())).isTrue();
        assertThat(LoginUserContext.get()).isNotNull();
        assertThat(LoginUserContext.get().getUserId()).isEqualTo(userIdOf("ctxuser"));

        jwtInterceptor.afterCompletion(request, response, new Object(), new RuntimeException("boom"));
        assertThat(LoginUserContext.get()).isNull();
    }
}
