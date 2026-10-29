package com.travelshare.common;

/**
 * 从 JWT 解析出的登录身份，只承载拦截器 → ThreadLocal → Controller 这一段链路需要的最小信息。
 */
public class LoginUser {

    private final Long userId;
    private final String role;

    public LoginUser(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
