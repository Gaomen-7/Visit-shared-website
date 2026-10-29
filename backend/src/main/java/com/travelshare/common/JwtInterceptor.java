package com.travelshare.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 三档鉴权拦截器（HLD §4.2）：取 Token → 校验签名与有效期 → userId/role 入 ThreadLocal → 路径级角色判断。
 *
 * <p>规则：
 * <ul>
 *   <li>公开档：无 Token 放行；带有效 Token 同样填充上下文，无效 Token 按游客处理。</li>
 *   <li>登录档：无 Token / 签名错 / 已过期 → 401。</li>
 *   <li>ADMIN 档：登录档之上要求 role = ADMIN，否则 403。</li>
 * </ul>
 *
 * <p>{@code /uploads/**} 不在 {@code /api/**} 下，天然不经过本拦截器——头像属公开呈现内容。
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String token = resolveToken(request);

        if (isPublicPath(path, request.getMethod())) {
            LoginUser loginUser = tryParse(token);
            if (loginUser != null) {
                LoginUserContext.set(loginUser);
            }
            return true;
        }

        LoginUser loginUser = tryParse(token);
        if (loginUser == null) {
            writeError(response, 401, "未登录或登录状态已失效");
            return false;
        }
        if (isAdminPath(path) && !loginUser.isAdmin()) {
            writeError(response, 403, "无权限访问该资源");
            return false;
        }

        LoginUserContext.set(loginUser);
        return true;
    }

    /**
     * 请求结束（含异常路径）无条件清空线程上下文，防止线程池复用串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        LoginUserContext.clear();
    }

    private LoginUser tryParse(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return jwtUtil.parse(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    private boolean isPublicPath(String path, String method) {
        if ("/api/ping".equals(path)) {
            return true;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if (path.startsWith("/api/destination/") || path.startsWith("/api/home/")) {
            return true;
        }
        // /api/article 读路径公开、写路径需登录；对应模块尚未实施，规则先按前缀配好。
        return ("/api/article".equals(path) || path.startsWith("/api/article/"))
                && "GET".equalsIgnoreCase(method);
    }

    private boolean isAdminPath(String path) {
        return "/api/admin".equals(path) || path.startsWith("/api/admin/");
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
    }
}
